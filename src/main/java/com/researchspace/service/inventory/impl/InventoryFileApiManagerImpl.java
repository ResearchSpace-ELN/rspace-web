package com.researchspace.service.inventory.impl;

import com.researchspace.core.util.MediaUtils;
import com.researchspace.dao.InventoryFileDao;
import com.researchspace.dao.SampleDao;
import com.researchspace.files.service.FileStore;
import com.researchspace.model.EcatMediaFile;
import com.researchspace.model.FileProperty;
import com.researchspace.model.User;
import com.researchspace.model.core.GlobalIdPrefix;
import com.researchspace.model.core.GlobalIdentifier;
import com.researchspace.model.inventory.InventoryFile;
import com.researchspace.model.inventory.InventoryRecord;
import com.researchspace.model.inventory.Sample;
import com.researchspace.model.inventory.field.SampleField;
import com.researchspace.model.record.IRecordFactory;
import com.researchspace.service.BaseRecordManager;
import com.researchspace.service.FileDuplicateStrategy;
import com.researchspace.service.FileStoreMetaManager;
import com.researchspace.service.MessageSourceUtils;
import com.researchspace.service.inventory.InventoryFileApiManager;
import com.researchspace.service.inventory.InventoryPermissionUtils;
import com.researchspace.service.inventory.InventoryRecordRetriever;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("inventoryFileApiManager")
@Slf4j
public class InventoryFileApiManagerImpl implements InventoryFileApiManager {

  private static final String INVENTORY_FILESTORE_CATEGORY = "inventory";

  private @Autowired InventoryFileDao inventoryFileDao;
  private @Autowired InventoryPermissionUtils invPermissions;
  private @Autowired SampleDao sampleDao;
  private @Autowired MessageSourceUtils messages;
  private @Autowired IRecordFactory recordFactory;
  private @Autowired BaseRecordManager baseRecordManager;

  @Autowired
  @Qualifier("compositeFileStore")
  private FileStore fileStore;

  @Autowired private FileStoreMetaManager fileStoreMetaManager;

  @Autowired private InventoryRecordRetriever inventoryRecordRetriever;

  @Override
  public boolean exists(long id) {
    return inventoryFileDao.exists(id);
  }

  @Override
  public InventoryFile getInventoryFileById(Long id, User user) {
    boolean exists = inventoryFileDao.exists(id);
    if (!exists) {
      throwNotFoundException(id, "Inventory file");
    }

    InventoryFile invFile = inventoryFileDao.getWithInitializedFields(id);
    GlobalIdentifier parentInvRecOid =
        getGlobalIdOfParentInventoryItem(invFile.getConnectedRecordOid());
    boolean canRead = invPermissions.canUserReadInventoryRecord(parentInvRecOid, user);
    if (!canRead) {
      throwNotFoundException(id, "Inventory file");
    }
    return invFile;
  }

  private void throwNotFoundException(Long id, String recordType) {
    String msg = messages.getResourceNotFoundMessage(recordType, id);
    throw new NotFoundException(msg);
  }

  @Override
  public InventoryFile attachNewInventoryFileToInventoryRecord(
      GlobalIdentifier globalIdToAttachTo,
      String originalFileName,
      InputStream inputStream,
      User user)
      throws IOException {

    GlobalIdentifier invItemOid = getGlobalIdOfParentInventoryItem(globalIdToAttachTo);
    InventoryRecord invRec = invPermissions.assertUserCanEditInventoryRecord(invItemOid, user);

    String filestoreName =
        String.format("att_%s_%s", globalIdToAttachTo.getIdString(), originalFileName);
    FileProperty fileProp = saveFileAndCreateFileProperty(user, filestoreName, inputStream);

    InventoryFile invFile = recordFactory.createInventoryFile(originalFileName, fileProp, user);
    invFile.setExtension(MediaUtils.getExtension(originalFileName));
    String mimeType = URLConnection.guessContentTypeFromName(originalFileName);
    if (mimeType == null) {
      mimeType = MediaUtils.getContentTypeForFileExtension(invFile.getExtension());
    }
    invFile.setContentMimeType(mimeType);

    return attachInventoryFileToInventoryRecord(invRec, invFile, globalIdToAttachTo);
  }

  @Override
  public InventoryFile attachGalleryFileToInventoryRecord(
      GlobalIdentifier globalIdToAttachTo, GlobalIdentifier galleryFileGlobalId, User user) {

    GlobalIdentifier invItemOid = getGlobalIdOfParentInventoryItem(globalIdToAttachTo);
    InventoryRecord invRec = invPermissions.assertUserCanEditInventoryRecord(invItemOid, user);

    EcatMediaFile galleryFile =
        baseRecordManager.retrieveMediaFile(user, galleryFileGlobalId.getDbId());
    InventoryFile invFile = new InventoryFile(galleryFile);

    return attachInventoryFileToInventoryRecord(invRec, invFile, globalIdToAttachTo);
  }

  private InventoryFile attachInventoryFileToInventoryRecord(
      InventoryRecord invRec, InventoryFile invFile, GlobalIdentifier globalIdToAttachTo) {

    if (GlobalIdPrefix.SF.equals(globalIdToAttachTo.getPrefix())) {
      Sample sample = (Sample) invRec;
      SampleField field = sample.getFieldById(globalIdToAttachTo.getDbId()).orElse(null);
      if (field == null) {
        throwNotFoundException(globalIdToAttachTo.getDbId(), "Sample field");
      }
      field.setAttachedFile(invFile);
    } else {
      invRec.addAttachedFile(invFile);
    }
    return inventoryFileDao.save(invFile);
  }

  /**
   * Finds global id of an inventory item based on invRecGlobalId.
   *
   * @param invRecGlobalId
   * @return
   */
  private GlobalIdentifier getGlobalIdOfParentInventoryItem(GlobalIdentifier invRecGlobalId) {
    if (GlobalIdPrefix.SF.equals(invRecGlobalId.getPrefix())) {
      GlobalIdentifier parentInvItemOid =
          sampleDao.getSampleGlobalIdFromFieldId(invRecGlobalId.getDbId());
      if (parentInvItemOid == null) {
        throwNotFoundException(invRecGlobalId.getDbId(), "Sample field");
      }
      return parentInvItemOid;
    }
    return invRecGlobalId;
  }

  private FileProperty saveFileAndCreateFileProperty(
      User user, String filestoreName, InputStream inputStream) throws IOException {
    // FileProperty.contentsHash is only relevant to images and the way they are retrieved therefore
    // set to an empty string here.
    String emptyContentsHash = "";
    return saveFileAndCreateFileProperty(user, filestoreName, emptyContentsHash, inputStream);
  }

  @Override
  public FileProperty saveFileAndCreateFileProperty(
      User user, String fileName, String contentsHash, InputStream inputStream) throws IOException {

    FileProperty fileProperty = new FileProperty();
    fileProperty.setFileCategory(INVENTORY_FILESTORE_CATEGORY);
    if (!user.getGroups().isEmpty()) {
      fileProperty.setFileGroup(user.getGroups().iterator().next().getUniqueName());
    } else {
      fileProperty.setFileGroup(user.getUsername());
    }
    fileProperty.setFileOwner(user.getUsername());
    fileProperty.setFileUser(user.getUsername());
    fileProperty.setFileVersion("1");
    fileProperty.setContentsHash(contentsHash);
    // we always save locally on initial upload
    fileProperty.setRoot(fileStore.getCurrentLocalFileStoreRoot());

    URI uri = fileStore.save(fileProperty, inputStream, fileName, FileDuplicateStrategy.AS_NEW);
    log.debug("File property {} created at URI {}", fileProperty.getId(), uri);
    log.info("URI is {}", fileProperty.getAbsolutePathUri());

    return fileProperty;
  }

  @Override
  public InventoryFile markInventoryFileAsDeleted(Long id, User user) {
    InventoryFile invFile = getInventoryFileById(id, user);
    GlobalIdentifier parentInvRecOid =
        getGlobalIdOfParentInventoryItem(invFile.getConnectedRecordOid());
    invPermissions.assertUserCanEditInventoryRecord(parentInvRecOid, user);

    // mark file as deleted
    invFile.setDeleted(true);
    invFile = inventoryFileDao.save(invFile);

    // refresh attachments list of parent record
    if (invFile.getInventoryRecord() != null) {
      invFile.getInventoryRecord().refreshActiveAttachedFiles();
    }

    return invFile;
  }

  public FileProperty getFilePropertyByContentsHash(String contentsHash, User user) {
    Map<String, String> properties = new HashMap<>();

    // for better performance to potentially avoid checking permissions, check if the user owns
    // any doc using the image with the contentsHash
    if (fileStoreMetaManager.doesUserOwnDocWithHash(user, contentsHash)) {
      return fileStoreMetaManager.getByHash(contentsHash);
    }

    // get any usage of the contents hash, rather than also being restricted by user, since e.g. the
    // image could belong to a template which wasn't created by the requesting user
    properties.put("contentsHash", contentsHash);
    List<FileProperty> filePropsWithHash = fileStoreMetaManager.findProperties(properties);

    if (filePropsWithHash.isEmpty()) {
      throw new NotFoundException(String.format("Image with hash %s not found.", contentsHash));
    }

    for (FileProperty fileProperty : filePropsWithHash) {
      if (inventoryRecordRetriever.userHasCanViewFileProperty(user, fileProperty)) {
        return fileProperty;
      }
    }

    // FileProperty exists, but the user doesn't have read permissions on any file which uses
    // the FileProperty
    throw new AuthorizationException(
        String.format(
            "User doesn't have permissions to read image " + "file with hash %s.", contentsHash));
  }
}
