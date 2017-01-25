package com.sap.hcpcu.persistence;

import com.sap.hcpcu.application.Service;

import com.sap.ui5.resource.util.IXSSEncoder;
import com.sap.ui5.resource.util.XSSEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.List;

import javax.persistence.EntityManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class RegistrationServlet
 */
public class RegistrationServlet extends HttpServlet {
  private static final long   serialVersionUID = 1L;

  /**
   * Logger for this class
   */
  private final static Logger log              = LoggerFactory.getLogger(RegistrationServlet.class);


  /**
   * @see HttpServlet#HttpServlet()
   */
  public RegistrationServlet() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.getWriter().println("<p>Persistence with JPA Sample!</p>");
    try {
      appendPersonTable(response);
      appendAddForm(response);
    } catch (Exception e) {
      response.getWriter().println("Persistence operation failed with reason: " + e.getMessage());
      log.error("! Persistence operation failed", e);
    }

    // TODO Auto-generated method stub
    response.getWriter().append("Served at: ").append(request.getContextPath());
  }


  /** {@inheritDoc} */
  @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      doAdd(request);
      doGet(request, response);
    } catch (Exception e) {
      response.getWriter().println("Persistence operation failed with reason: " + e.getMessage());
      log.error("! Persistence operation failed", e);
    }
  }


  private void appendAddForm(HttpServletResponse response) throws IOException {
    // Append form through which new persons can be added
    response.getWriter().println("<p><form action=\"\" method=\"post\">" + "First name:<input type=\"text\" name=\"Firstname\">" + "&nbsp;Last name:<input type=\"text\" name=\"Lastname\">" + "&nbsp;<input type=\"submit\" value=\"Add Registration\">" + "</form></p>");
  }


  private void appendPersonTable(HttpServletResponse response) throws IOException {
    // Append table that lists all persons
    EntityManager em = Service.getDatabasePool().getEntityManager();

    try {
      @SuppressWarnings("unchecked")
      List<Registration> resultList = em.createNamedQuery("Registrations.findAll").getResultList();
      response.getWriter().println("<p><table border=\"1\"><tr><th colspan=\"3\">" + (resultList.isEmpty() ? "" : (resultList.size() + " ")) + "Entries in the Database</th></tr>");
      if (resultList.isEmpty()) {
        response.getWriter().println("<tr><td colspan=\"3\">Database is empty</td></tr>");
      } else {
        response.getWriter().println("<tr><th>First name</th><th>Last name</th><th>Id</th></tr>");
      }

      IXSSEncoder xssEncoder = XSSEncoder.getInstance();
      for (Registration p : resultList) {
        response.getWriter().println("<tr><td>" + xssEncoder.encodeHTML(p.getFirstname()) + "</td><td>" + xssEncoder.encodeHTML(p.getLastname()) + "</td><td>" + p.getId() + "</td></tr>");
      }

      response.getWriter().println("</table></p>");
    } finally {
      em.close();
    }
  }


  private void doAdd(HttpServletRequest request) throws ServletException, IOException {
    // Extract name of person to be added from request
    String        firstName = request.getParameter("Firstname");
    String        lastName  = request.getParameter("Lastname");

    // Add person if name is not null/empty
    EntityManager em        = Service.getDatabasePool().getEntityManager();
    try {
      if ((firstName != null) && (lastName != null) && !firstName.trim().isEmpty() && !lastName.trim().isEmpty()) {
        Registration registration = new Registration();
        registration.setFirstname(firstName);
        registration.setLastname(lastName);
        em.getTransaction().begin();
        em.persist(registration);
        em.getTransaction().commit();
      }
    } finally {
      em.close();
    }
  }
}
