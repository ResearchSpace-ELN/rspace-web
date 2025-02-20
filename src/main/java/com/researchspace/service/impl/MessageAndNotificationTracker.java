package com.researchspace.service.impl;

import com.researchspace.model.comms.MessageType;
import com.researchspace.model.dtos.NotificationStatus;
import com.researchspace.service.IMessageAndNotificationTracker;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class holding status for users as to whether that user has new notifications or not. <br>
 * This is is to hold in memory information so that polling from client does not have to do DB
 * lookups.
 *
 * @see IMessageAndNotificationTracker
 */
public class MessageAndNotificationTracker implements IMessageAndNotificationTracker {

  private Map<Long, Integer> userToNotification = new ConcurrentHashMap<>();
  private Map<Long, Integer> userToMessage = new ConcurrentHashMap<>();
  private Map<Long, Integer> userToSpecialMessage = new ConcurrentHashMap<>();

  public int changeUserNotificationCount(Long userId, Integer incrementAmount) {
    return doChangeCount(userId, incrementAmount, userToNotification);
  }

  public void clearUserNotificationCount(Long userId) {
    userToNotification.put(userId, 0);
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
    return doChangeCount(userId, incrementAmount, userToSpecialMessage);
  }

  private int changeUserMessageCount(Long userId, int incrementAmount) {
    return doChangeCount(userId, incrementAmount, userToMessage);
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
  }
}
