package com.researchspace.dao.hibernate;

import com.researchspace.dao.GenericDaoHibernate;
import com.researchspace.dao.RSChemElementDao;
import com.researchspace.model.ChemElementsFormat;
import com.researchspace.model.RSChemElement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.QueryHints;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

@Repository("RSChemElementDaoHibernate")
public class RSChemElementDaoHibernate extends GenericDaoHibernate<RSChemElement, Long>
    implements RSChemElementDao {

  public RSChemElementDaoHibernate() {
    super(RSChemElement.class);
  }

  /**
   * @param fieldId the db identifier of the record containing this annotation
   * @return A possibly empty but non-<code>null</code> list of EcatImageAnnotation objects for this
   *     field.
   */
  public List<RSChemElement> getAllChemElementsFromField(Long fieldId) {
    Query<RSChemElement> sq =
        getSession()
            .createQuery(
                " from RSChemElement chem where chem.parentId = :parentId ", RSChemElement.class);
    sq.setParameter("parentId", fieldId);
    return sq.list();
  }

  public List<RSChemElement> getChemElementsForReadOnlyAndClearDBSession(List<Long> chemIds) {
    List<RSChemElement> result = new ArrayList<>();
    if (!CollectionUtils.isEmpty(chemIds)) {
      Query<RSChemElement> chemicalQuery =
          getSession()
              .createQuery(
                  " from RSChemElement chem where chem.id in (:chemicalIds) ", RSChemElement.class);
      chemicalQuery.setParameterList("chemicalIds", chemIds);
      chemicalQuery.setHint(QueryHints.READ_ONLY, true);
      result.addAll(chemicalQuery.list());
      getSession().clear();
    }
    return result;
  }

  @Override
  public List<RSChemElement> getChemElementsFromChemFileId(Long ecatChemFileId) {
    Query<RSChemElement> sq =
        getSession()
            .createQuery(
                " from RSChemElement chem where chem.ecatChemFileId = :ecatChemFileId",
                RSChemElement.class);
    sq.setParameter("ecatChemFileId", ecatChemFileId);
    return sq.list();
  }

  @Override
  public RSChemElement getChemElementFromChemistryGalleryFile(Long ecatChemFileId) {
    Query<RSChemElement> sq =
        getSession()
            .createQuery(
                " from RSChemElement chem where chem.ecatChemFileId = :ecatChemFileId and"
                    + " chem.parentId = null",
                RSChemElement.class);
    sq.setParameter("ecatChemFileId", ecatChemFileId);
    return sq.uniqueResult();
  }

  @Override
  public List<Object[]> getAllIdAndSmilesStringPairs() {
    return getSession().createNativeQuery("select id, smilesString from RSChemElement").list();
  }

  @Override
  public List<RSChemElement> getAllChemicalsWithFormat(ChemElementsFormat format) {
    Query<RSChemElement> query =
        getSession()
            .createQuery(
                " from RSChemElement chem where chem.chemElementsFormat = :format",
                RSChemElement.class);
    query.setParameter("format", format);
    return query.list();
  }
}
