
(function () {
if (window.__AI_WIDGET_LOADED__) return;
window.__AI_WIDGET_LOADED__ = true;
  const tenantId =
    document.currentScript.getAttribute("data-tenant-id") || "default";

  const position =
    document.currentScript.getAttribute("data-position") || "right-bottom";

  /* UI */
    const html = `
      <div id="ai-widget-icon" class="${position}">💬</div>

      <div id="ai-widget-box" class="${position}">
        <div id="ai-widget-header">
          AI Assistant
          <span id="ai-widget-close">✖</span>
        </div>
        <div id="ai-widget-messages"></div>
        <div id="ai-widget-input">
          <input id="ai-input" placeholder="Type a message..." />
          <button id="ai-send">➤</button>
        </div>
      </div>
    `;


  document.body.insertAdjacentHTML("beforeend", html);

  /* CSS */
  const css = document.createElement("link");
  css.rel = "stylesheet";
  css.href = "http://localhost:8080/widget/widget.css";
  document.head.appendChild(css);

  /* Elements */
  const icon = document.getElementById("ai-widget-icon");
  const box = document.getElementById("ai-widget-box");
  const close = document.getElementById("ai-widget-close");
  const send = document.getElementById("ai-send");
  const input = document.getElementById("ai-input");
  const messages = document.getElementById("ai-widget-messages");

  icon.onclick = () => box.style.display = "flex";
  close.onclick = () => box.style.display = "none";

  async function sendMessage() {
    const q = input.value.trim();
    if (!q) return;

    add("user", q);
    input.value = "";

    add("bot", "⏳ Thinking...");

    const res = await fetch(
      `/ai-bot/api/v1/chat?query=${encodeURIComponent(q)}`,
      { headers: { tenantId } }
    );

    const text = await res.text();
    messages.lastChild.innerText = text;
  }

  send.onclick = sendMessage;
  input.addEventListener("keydown", function (e) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  function add(type, text) {
    const div = document.createElement("div");
    div.className = "msg " + type;
    div.innerText = text;
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
  }

})();
