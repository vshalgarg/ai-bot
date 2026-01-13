async function uploadPdf() {
  const fileInput = document.getElementById("pdfFile");
  const status = document.getElementById("uploadStatus");
  const tenantId = document.getElementById("tenantId");

  if (!fileInput.files.length) {
    status.innerText = "❌ Please select a PDF file";
    return;
  }

  const formData = new FormData();
  formData.append("file", fileInput.files[0]);

  status.innerText = "⏳ Uploading & processing PDF...";

  try {
    const res = await fetch("/ai-bot/api/v1/ingest", {
      method: "POST",
      body: formData,
      headers: {
        "tenantId": tenantId
      }
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
  const tenantId = document.getElementById("tenantId");

  answer.innerText = "⏳ Thinking...";

  const res = await fetch("/ai-bot/api/v1/chat?query="+q, {
    method: "GET",
    headers: {"Content-Type":"application/json", "tenantId": tenantId},
  });

  answer.innerText = await res.text();
}
