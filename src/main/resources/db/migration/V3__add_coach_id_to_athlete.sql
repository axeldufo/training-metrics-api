ALTER TABLE athlete ADD COLUMN coach_id BIGINT NOT NULL;
ALTER TABLE athlete ADD CONSTRAINT fk_athlete_coach FOREIGN KEY (coach_id) REFERENCES coach(id);
