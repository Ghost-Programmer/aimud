CREATE INDEX IF NOT EXISTS vector_store_world_lore_idx 
ON vector_store 
USING HNSW (embedding vector_cosine_ops) 
WITH (m=32, ef_construction=128) 
WHERE metadata->>'namespace' = 'world_lore';
