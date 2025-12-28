package com.teamboard.entity;

public enum MemberRole {
  ADMIN("Admin"),
  MEMBER("Member"),
  VIEWER("Viewer");

  private final String displayName;

  MemberRole(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

