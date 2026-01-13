async function uploadPdf() {
  const fileInput = document.getElementById("pdfFile");
  const status = document.getElementById("uploadStatus");
  const tenantId = document.getElementById("tenantId").value;

  if (!tenantId) {
    status.innerText = "❌ Tenant ID is required";
    return;
  }

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
      headers: { tenantId }
    });

    const text = await res.text();
    status.innerText = "✅ " + text;
  } catch (e) {
    status.innerText = "❌ Upload failed";
  }
}
