ALTER TABLE queues ALTER COLUMN updated DROP NOT NULL;
ALTER TABLE queues ALTER COLUMN updated DROP DEFAULT;