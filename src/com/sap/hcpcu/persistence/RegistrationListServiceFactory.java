package com.sap.hcpcu.persistence;

import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

import com.sap.hcpcu.application.Service;

import javax.persistence.EntityManagerFactory;


public class RegistrationListServiceFactory extends ODataJPAServiceFactory {
  @Override public ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException {
    final ODataJPAContext oDataJPAContext = this.getODataJPAContext();
    try {
      final EntityManagerFactory emf = Service.getDatabasePool().getEntityManagerFactory();
      oDataJPAContext.setEntityManagerFactory(emf);
      oDataJPAContext.setPersistenceUnitName(Service.getDatabasePool().getPersistenceUnit());

      return oDataJPAContext;
    } catch (Exception e) {
      throw (new RuntimeException(e));
    }
  }
}
