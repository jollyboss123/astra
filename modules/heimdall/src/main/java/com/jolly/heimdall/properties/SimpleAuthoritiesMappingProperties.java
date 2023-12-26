package com.jolly.heimdall.properties;

/**
 * @author jolly
 */
public class SimpleAuthoritiesMappingProperties {
  private String path = "$.realm_access.roles";
  private String prefix = "";
  private Case caseProcessing = Case.UNCHANGED;

  public enum Case {
    UNCHANGED, UPPER, LOWER
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public Case getCaseProcessing() {
    return caseProcessing;
  }

  public void setCaseProcessing(Case caseProcessing) {
    this.caseProcessing = caseProcessing;
  }
}
