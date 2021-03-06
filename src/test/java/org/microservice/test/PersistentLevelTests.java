package org.microservice.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.microservice.dao.DMLType;
import org.microservice.dao.OperationFactory;
import org.microservice.model.Cached;
import org.microservice.model.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {TestContext.class})
@Transactional
public class PersistentLevelTests {

	private static final Logger logger = LoggerFactory.getLogger(PersistentLevelTests.class);
	
	@Autowired
	private EntityManagerFactory entityManagerFactory;
	
	private OperationFactory operationFactory;
	private Session session;
	
	@Before
	public void init(){
		operationFactory = new OperationFactory();
		session = entityManagerFactory.unwrap(SessionFactory.class).openSession(); 
	}


	@Test
    @Rollback(true)
	public void upsertTest() {
		String key = "key1";
		String value = "val1";
		Pair pair = new Pair(key,value);
		assertTrue(entityManagerFactory != null);
		assertTrue(session != null);
		Transaction tr = null;
		tr = session.beginTransaction();
		DMLType result = (DMLType) operationFactory.upsert(session, pair);

		assertTrue(result != null);
		
		Cached found = (Cached) operationFactory.searchByKey(session, key);
		tr.commit();
		
		assertTrue(found != null);
		assertTrue(found.getKey() != null);
		assertTrue(found.getKey().equals(key));
		assertTrue(found.getValue() != null);
		assertTrue(found.getValue().equals(value));

	}

	@Test
    @Rollback(true)
	public void recordsCollectorTest() {
		String key = "key2";
		String value = "val2";
		Pair pair = new Pair(key,value);
		assertTrue(entityManagerFactory != null);
		assertTrue(session != null);
		Transaction tr = null;
		tr = session.beginTransaction();
		DMLType result = (DMLType) operationFactory.upsert(session, pair);
		assertTrue(result != null);
		
		Cached found = (Cached) operationFactory.searchByKey(session, key);
		assertTrue(found != null);

		
		long foundExpired = operationFactory.countExpired(session, 0L);
		logger.debug("foundExpired="+foundExpired);
		assertNotEquals(0L,foundExpired);
		
		int deletedExpired = operationFactory.deleteExpired(session, 0L);
		logger.debug("deletedExpired="+deletedExpired);
		assertNotEquals(0,deletedExpired);
		tr.commit();

	}

}
