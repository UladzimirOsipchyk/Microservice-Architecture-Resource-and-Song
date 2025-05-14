package com.example.resourceservice.enums;

import lombok.Getter;

@Getter
public enum StorageType {
  STAGING("STAGING"),
  PERMANENT("PERMANENT");

  StorageType(String type) {
    this.type = type;
  }

  private final String type;
}
