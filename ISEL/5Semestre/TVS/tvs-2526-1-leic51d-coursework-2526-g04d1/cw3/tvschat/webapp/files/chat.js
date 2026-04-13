(() => {
  let ctx = null;

  const convo = document.getElementById('conversation');
  const input = document.getElementById('userInput');
  const sendBtn = document.getElementById('sendBtn');

  input.focus();

  function escapeHtml(str) {
    return String(str)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;');
  }

  function appendMessage(role, text) {
    const safe = escapeHtml(text).replaceAll('\r', '').replaceAll('\n', '<br>');
    const div = document.createElement('div');
    div.className = 'msg msg-' + role.toLowerCase();
    div.innerHTML = `<strong>${role}:</strong> ${safe}<br>`;
    convo.appendChild(div);
    convo.scrollTop = convo.scrollHeight;
  }

  async function sendMessage() {
    const msg = (input.value || '').trim();
    if (!msg) return;

    appendMessage('You', msg);
    input.value = '';

    try {
      const r = await fetch('chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: msg, context: ctx })
      });

      if (!r.ok) {
        const t = await r.text().catch(() => '');
        appendMessage('Error', `HTTP ${r.status}: ${t.slice(0, 200)}`);
        return;
      }

      const j = await r.json();
      if (j.response) appendMessage('Assistant', j.response);
      if (j.context) ctx = j.context;
    } catch (e) {
      appendMessage('Error', String(e));
    }

    input.focus()
  }

  sendBtn.addEventListener('click', sendMessage);
  input.addEventListener('keydown', (ev) => {
    if (ev.key === 'Enter' && (ev.ctrlKey || ev.metaKey)) {
      ev.preventDefault();
      sendMessage();
    }
  });
})();

