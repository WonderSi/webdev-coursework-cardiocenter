CREATE TABLE admin_user_sections (
    admin_user_id BIGINT NOT NULL
        REFERENCES admin_users (id) ON DELETE CASCADE,
    section VARCHAR(30) NOT NULL,
    PRIMARY KEY (admin_user_id, section)
);

INSERT INTO admin_user_sections (admin_user_id, section)
SELECT users.id, sections.section
FROM admin_users AS users
CROSS JOIN (
    VALUES
        ('DASHBOARDS'),
        ('UPLOAD_DATA'),
        ('MODEL_TRAINING'),
        ('VIEW_DATA')
) AS sections(section)
WHERE users.role = 'DOCTOR';