package com.researchspace.webapp.integrations.wopi;

import static org.junit.Assert.assertEquals;

import com.researchspace.service.MediaFileLockHandler;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;

public class WopiLocksHandlerTest {

  private MediaFileLockHandler lockHandler = new MediaFileLockHandler();

  @Test
  public void testLockRelockUnlockSequence() throws IOException {

    String fileId = "GL12";
    String lockId = "lock1";
    String lockId2 = "lock2";

    // assert no lock
    assertEquals("", lockHandler.getLock(fileId));

    // requesting lock for user/resource returns this lock id
    String returnedLockMismatchedValue = lockHandler.lock(fileId, lockId);
    assertEquals(null, returnedLockMismatchedValue);
    assertEquals(lockId, lockHandler.getLock(fileId));

    // requesting lock with different id for the same user/resource returns old lock id
    returnedLockMismatchedValue = lockHandler.lock(fileId, lockId2);
    assertEquals(lockId, returnedLockMismatchedValue);
    assertEquals(lockId, lockHandler.getLock(fileId));

    // requesting re-lock with different id for the same user/resource returns new lock id
    returnedLockMismatchedValue = lockHandler.unlockAndRelock(fileId, lockId, lockId2);
    assertEquals(null, returnedLockMismatchedValue);
    assertEquals(lockId2, lockHandler.getLock(fileId));

    // unlocking
    returnedLockMismatchedValue = lockHandler.unlock(fileId, lockId2);
    assertEquals(null, returnedLockMismatchedValue);
    assertEquals("", lockHandler.getLock(fileId));
  }

  @Test
  public void testLockRefreshingAndExpiring() throws Exception {
    // Use a mutable clock that we advance manually — no Thread.sleep needed
    AtomicLong epochMs = new AtomicLong(1_000_000L);
    Clock fakeClock =
        new Clock() {
          @Override
          public ZoneOffset getZone() {
            return ZoneOffset.UTC;
          }

          @Override
          public Clock withZone(java.time.ZoneId zone) {
            return this;
          }

          @Override
          public Instant instant() {
            return Instant.ofEpochMilli(epochMs.get());
          }
        };
    lockHandler.setClock(fakeClock);
    lockHandler.setLockDurationInMilis(1000); // 1 000 ms lock duration

    String fileId = "GL12";
    String fileId2 = "GL122";
    String lockId = "lock1";
    String lockId2 = "lock12";

    // lock both resources at t=0
    lockHandler.lock(fileId, lockId);
    lockHandler.lock(fileId2, lockId2);
    assertEquals(lockId, lockHandler.getLock(fileId));
    assertEquals(lockId2, lockHandler.getLock(fileId2));

    // advance 500 ms, refresh lock on fileId2
    epochMs.addAndGet(500);
    lockHandler.refreshLock(fileId2, lockId2);
    assertEquals(lockId, lockHandler.getLock(fileId)); // not yet expired
    assertEquals(lockId2, lockHandler.getLock(fileId2));

    // advance another 500 ms (total 1000 ms) — fileId lock should expire
    epochMs.addAndGet(500);
    assertEquals("", lockHandler.getLock(fileId)); // expired
    assertEquals(lockId2, lockHandler.getLock(fileId2)); // refreshed at t=500, still valid

    // advance another 500 ms (total 1500 ms) — fileId2 lock expires too
    epochMs.addAndGet(500);
    assertEquals("", lockHandler.getLock(fileId));
    assertEquals("", lockHandler.getLock(fileId2));
  }
}
