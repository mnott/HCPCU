package com.sap.hcpcu.persistence;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "registration")
@NamedQueries({ @NamedQuery(name = "Registrations.findAll", query = "select p from Registration p") })
public class Registration {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id private long  id;

  @Column(name = "ts", insertable = false, updatable = false)
  private Timestamp ts;

  private String    company;

  private String    firstname;

  private String    lastname;

  private String    email;

  private int       workshop;

  private String    workshopDate;

  private String    scenario;

  public Registration() {}


  public Registration(final String company, final String firstname, final String lastname, final String email, final int workshop, final String workshopDate, final String scenario) {
    this.company      = company;
    this.firstname    = firstname;
    this.lastname     = lastname;
    this.email        = email;
    this.workshop     = workshop;
    this.workshopDate = workshopDate;
    this.scenario     = scenario;
  }

  public long getId() {
    return id;
  }


  public void setId(long id) {
    this.id = id;
  }


  public Timestamp getTs() {
    return ts;
  }


  public void setTs(Timestamp ts) {
    this.ts = ts;
  }


  public String getCompany() {
    return company;
  }


  public void setCompany(String company) {
    this.company = company;
  }


  public String getFirstname() {
    return firstname;
  }


  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }


  public String getLastname() {
    return lastname;
  }


  public void setLastname(String lastname) {
    this.lastname = lastname;
  }


  public String getEmail() {
    return email;
  }


  public void setEmail(String email) {
    this.email = email;
  }


  public int getWorkshop() {
    return workshop;
  }


  public void setWorkshop(int workshop) {
    this.workshop = workshop;
  }


  public String getWorkshopDate() {
    return workshopDate;
  }


  public void setWorkshopDate(String workshopDate) {
    this.workshopDate = workshopDate;
  }


  public String getScenario() {
    return scenario;
  }


  public void setScenario(String scenario) {
    this.scenario = scenario;
  }


  @Override public String toString() {
    return String.format("(%s, %s)", this.firstname, this.lastname);
  }
}
