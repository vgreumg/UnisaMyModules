ALTER TABLE BBB_MEETING ADD COLUMN PRESENTATION VARCHAR(255) AFTER MULTIPLE_SESSIONS_ALLOWED;
ALTER TABLE BBB_MEETING ADD COLUMN GROUP_SESSIONS BOOLEAN AFTER PRESENTATION;
