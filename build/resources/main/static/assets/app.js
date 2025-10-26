// Simple API wrapper for card endpoints
const CardApi = {
  async addCard(payload) {
    const response = await fetch("/api/cards", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const message = await CardApi._readError(response);
      throw new Error(message);
    }

    return CardApi._readJson(response);
  },

  async listCards({ last4 } = {}) {
    const query = last4 ? `?last4=${encodeURIComponent(last4)}` : "";
    const response = await fetch(`/api/cards${query}`);

    if (!response.ok) {
      const message = await CardApi._readError(response);
      throw new Error(message);
    }

    return CardApi._readJson(response);
  },

  async _readJson(response) {
    const text = await response.text();
    if (!text) {
      return null;
    }

    try {
      return JSON.parse(text);
    } catch (error) {
      return text;
    }
  },

  async _readError(response) {
    const text = await response.text();
    if (!text) {
      return `Request failed with status ${response.status}`;
    }

    try {
      const parsed = JSON.parse(text);
      return parsed.message || JSON.stringify(parsed);
    } catch (error) {
      return text;
    }
  },
};

const CardUi = {
  showMessage(target, message, type = "success") {
    if (!target) return;
    target.textContent = message;
    target.className = `message ${type}`;
  },

  clearMessage(target) {
    if (!target) return;
    target.textContent = "";
    target.className = "message";
  },

  formatMaskedPan(card) {
    if (!card) return "";
    if (card.maskedPan) {
      return card.maskedPan;
    }

    if (card.pan || card.cardNumber) {
      const value = card.pan || card.cardNumber;
      const digits = value.replace(/\D/g, "");
      if (digits.length >= 4) {
        const masked = digits
          .slice(-4)
          .padStart(digits.length, "*")
          .match(/.{1,4}/g)
          .join(" ");
        return masked;
      }
      return digits;
    }

    return "";
  },

  formatTimestamp(timestamp) {
    if (!timestamp) return "";
    const date = typeof timestamp === "string" ? new Date(timestamp) : timestamp;
    if (Number.isNaN(date.getTime())) {
      return timestamp;
    }
    return date.toLocaleString();
  },
};

window.CardApi = CardApi;
window.CardUi = CardUi;
