package com.jolly.heimdall.properties;

/**
 * @author jolly
 */
public class SimpleAuthoritiesMappingProperties {
  /**
   * JSON path of the claim(s) to map with this properties.
   */
  private String path = "$.realm_access.roles";
  /**
   * What to prefix authorities with (for instance "ROLE_" or "SCOPE_").
   */
  private String prefix = "";
  /**
   * Whether to transform authorities to uppercase, lowercase, or unchanged.
   */
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
