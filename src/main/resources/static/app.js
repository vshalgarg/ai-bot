async function uploadPdf() {
  const fileInput = document.getElementById("pdfFile");
  const status = document.getElementById("uploadStatus");

  if (!fileInput.files.length) {
    status.innerText = "❌ Please select a PDF file";
    return;
  }

  const formData = new FormData();
  formData.append("file", fileInput.files[0]);

  status.innerText = "⏳ Uploading & processing PDF...";

  try {
    const res = await fetch("/api/ingest", {
      method: "POST",
      body: formData
    });

    const text = await res.text();
    status.innerText = "✅ " + text;
  } catch (e) {
    status.innerText = "❌ Upload failed";
  }
}

async function ask() {
  const q = document.getElementById("question").value;
  const answer = document.getElementById("answer");

  answer.innerText = "⏳ Thinking...";

  const res = await fetch("/api/chat?query="+q, {
    method: "GET",
    headers: {"Content-Type":"application/json"},
  });

  answer.innerText = await res.text();
}
