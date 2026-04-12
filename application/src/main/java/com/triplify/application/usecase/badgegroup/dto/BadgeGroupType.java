package com.triplify.application.usecase.badgegroup.dto;

public enum BadgeGroupType {
	COUNTRIES("10000000-0000-0000-0000-000000000001"),
	KILOMETERS("10000000-0000-0000-0000-000000000002"),
	TRIPS("10000000-0000-0000-0000-000000000003"),
	ROUTES("10000000-0000-0000-0000-000000000004"),
	PLACES("10000000-0000-0000-0000-000000000005"),
	STORIES("10000000-0000-0000-0000-000000000006"),
	PHOTOS("10000000-0000-0000-0000-000000000007");

	private final String id;

	BadgeGroupType(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static BadgeGroupType fromIdOrThrow(String id) {
		for (BadgeGroupType group : values()) {
			if (group.id.equals(id)) {
				return group;
			}
		}
		throw new IllegalArgumentException("Unknown badge group id: " + id);
	}
}

