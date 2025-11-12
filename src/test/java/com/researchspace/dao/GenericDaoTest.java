package com.researchspace.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.researchspace.model.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;

public class GenericDaoTest extends BaseDaoTestCase {
  Logger log = LoggerFactory.getLogger(GenericDaoTest.class);
  GenericDao<User, Long> genericDao;
  @Autowired SessionFactory sessionFactory;

  @BeforeEach
  public void setUp() {
    genericDao = new GenericDaoHibernate<User, Long>(User.class, sessionFactory);
  }

  @Test
  public void getUser() {
    User user = genericDao.get(-1L);
    assertNotNull(user);
    assertEquals("user1a", user.getUsername());
  }

  @Test
  public void testGetThrowsExceptionIfObjectNotFound() {
    assertThrows(
        ObjectRetrievalFailureException.class,
        () -> {
          final Long UNKNOWNID = -1234556L;
          // returns null
          assertFalse(genericDao.getSafeNull(UNKNOWNID).isPresent());
          // throws exception
          User user = genericDao.get(-UNKNOWNID); //
        }); //
  }
}
