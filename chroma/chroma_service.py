from flask import Flask, request
import chromadb

app = Flask(__name__)
client = chromadb.Client()
col = client.get_or_create_collection("kb")

@app.post("/add")
def add():
    d = request.json
    col.add(documents=d["documents"], embeddings=d["embeddings"], ids=d["ids"])
    return {"ok": True}

@app.post("/query")
def query():
    e = request.json["embedding"]
    return col.query(query_embeddings=[e], n_results=4)

app.run(host="0.0.0.0", port=5000)
