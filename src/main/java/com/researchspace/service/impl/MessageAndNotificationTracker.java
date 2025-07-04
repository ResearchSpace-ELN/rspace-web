package com.researchspace.service.impl;

import com.researchspace.model.comms.MessageType;
import com.researchspace.model.dtos.NotificationStatus;
import com.researchspace.service.IMessageAndNotificationTracker;
import com.researchspace.webapp.messaging.NotificationMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Tracks message and notification counts for users. When new notifications or messages are
 * generated within the system, this service publishes the updated counts to a topic for each user.
 */
@Service
public class MessageAndNotificationTracker implements IMessageAndNotificationTracker {

  @Autowired private SimpMessagingTemplate messagingTemplate;

  private Map<Long, Integer> userToNotification = new ConcurrentHashMap<>();
  private Map<Long, Integer> userToMessage = new ConcurrentHashMap<>();
  private Map<Long, Integer> userToSpecialMessage = new ConcurrentHashMap<>();

  public void sendNotificationUpdate(Long userId) {
    NotificationMessage notification =
        new NotificationMessage(
            getNotificationCountFor(userId),
            getMessageCountFor(userId),
            getSpecialMessageCountFor(userId));
    sendNotificationUpdate(userId, notification);
  }

  public int changeUserNotificationCount(Long userId, Integer incrementAmount) {
    int newCount = doChangeCount(userId, incrementAmount, userToNotification);
    sendNotificationUpdate(userId);
    return newCount;
  }

  public void clearUserNotificationCount(Long userId) {
    userToNotification.put(userId, 0);
    sendNotificationUpdate(userId);
  }

  public Boolean userHasNewNotifications(Long userId) {
    Integer rc = userToNotification.get(userId);
    if (rc == null || rc == 0) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  public Integer getNotificationCountFor(Long userId) {
    return doGetCount(userId, userToNotification);
  }

  public Boolean userHasActiveMessages(Long userId) {
    return doActiveMessages(userId, userToMessage);
  }

  public Integer getMessageCountFor(Long userId) {
    return doGetCount(userId, userToMessage);
  }

  @Override
  public Integer getSpecialMessageCountFor(Long userId) {
    return doGetCount(userId, userToSpecialMessage);
  }

  @Override
  public void updateMessageCount(Long id, int increment, MessageType messageType) {
    if (messageType.isStandardType()) {
      changeUserMessageCount(id, increment);
    } else {
      changeUserSpecialMsgCount(id, increment);
    }
  }

  private int changeUserSpecialMsgCount(Long userId, Integer incrementAmount) {
    int newCount = doChangeCount(userId, incrementAmount, userToSpecialMessage);
    sendNotificationUpdate(userId);
    return newCount;
  }

  private int changeUserMessageCount(Long userId, int incrementAmount) {
    int newCount = doChangeCount(userId, incrementAmount, userToMessage);
    sendNotificationUpdate(userId);
    return newCount;
  }

  private int doChangeCount(Long userId, int incrementAmount, Map<Long, Integer> userToCount) {
    Integer crrCount = userToCount.get(userId);
    if (crrCount == null) {
      userToCount.put(userId, incrementAmount);
      return incrementAmount;
    } else {
      int newAmount = crrCount + incrementAmount;
      // never < 0
      newAmount = (newAmount < 0) ? 0 : newAmount;
      userToCount.put(userId, newAmount);
      return newAmount;
    }
  }

  private Integer doGetCount(Long userId, Map<Long, Integer> userToCount) {
    Integer rc = userToCount.get(userId);
    if (rc == null || rc == 0) {
      rc = 0;
    }
    return rc;
  }

  private boolean doActiveMessages(Long userId, Map<Long, Integer> userToCount) {
    Integer rc = userToCount.get(userId);
    if (rc == null || rc == 0) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  @Override
  public void initCount(Long userId, NotificationStatus notificationStatus) {
    userToNotification.put(userId, notificationStatus.getNotificationCount());
    userToMessage.put(userId, notificationStatus.getMessageCount());
    userToSpecialMessage.put(userId, notificationStatus.getSpecialMessageCount());
    sendNotificationUpdate(userId);
  }

  public void sendNotificationUpdate(Long userId, NotificationMessage notification) {
    messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
  }
}
