// from anticon
const CardIcons = {
  creditCard:
    '<span class="anticon anticon-credit-card" aria-hidden="true"><svg viewBox="64 64 896 896" focusable="false" data-icon="credit-card" width="1em" height="1em" fill="currentColor" aria-hidden="true"><path d="M880 298H144c-17.7 0-32 14.3-32 32v364c0 17.7 14.3 32 32 32h736c17.7 0 32-14.3 32-32V330c0-17.7-14.3-32-32-32zm-8 388H152V438h720v248zm0-308H152v-48h720v48zM344 562h248v60H344z"></path></svg></span>',
  search:
    '<span class="anticon anticon-search" aria-hidden="true"><svg viewBox="64 64 896 896" focusable="false" data-icon="search" width="1em" height="1em" fill="currentColor" aria-hidden="true"><path d="M909.6 854.5L701.9 646.8A318.4 318.4 0 00712 512c0-176.7-143.3-320-320-320S72 335.3 72 512s143.3 320 320 320c73 0 140.1-24.3 194.1-65.2l208.6 207.7a8 8 0 0011.3 0l82-82a8 8 0 000-11.3zM392 704c-106 0-192-86-192-192s86-192 192-192 192 86 192 192-86 192-192 192z"></path></svg></span>',
  plus:
    '<span class="anticon anticon-plus" aria-hidden="true"><svg viewBox="64 64 896 896" focusable="false" data-icon="plus" width="1em" height="1em" fill="currentColor" aria-hidden="true"><path d="M482 152h60v270h270v60H542v270h-60V482H212v-60h270z"></path></svg></span>',
};

// API wrapper for card endpoints
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
    if (card.lastFour) {
      return `**** **** **** ${card.lastFour}`;
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

  initDashboard() {
    const totalCardsEl = document.getElementById("totalCards");
    const quickActionButtons = document.querySelectorAll(
      ".quick-action[data-panel]"
    );
    const panels = {
      add: document.getElementById("addPanel"),
      search: document.getElementById("searchPanel"),
    };

    const addForm = document.getElementById("addCardForm");
    const addMessage = document.getElementById("formMessage");
    const cardholderInput = document.getElementById("cardholderName");
    const panInput = document.getElementById("cardNumber");

    const searchForm = document.getElementById("searchForm");
    const last4Input = document.getElementById("last4");
    const searchMessage = document.getElementById("searchMessage");
    const resultsBody = document.getElementById("resultsBody");
    const recordsCountEl = document.getElementById("recordsCount");
    const resultsSubtitleEl = document.getElementById("resultsSubtitle");

    const state = {
      totalCards: 0,
      lastSearch: null,
      lastResults: [],
      lastLabel: "Use the search form to load card records.",
      resultsInitialized: false,
    };

    function normalizeCardsResponse(payload) {
      if (!payload) return [];
      if (Array.isArray(payload)) return payload;
      if (Array.isArray(payload.content)) return payload.content;
      return [];
    }

    function setTotals(count) {
      if (totalCardsEl) {
        totalCardsEl.textContent = count;
      }
    }

    function renderResults(cards, filterLabel) {
      if (!resultsBody) {
        return;
      }

      state.resultsInitialized = true;
      resultsBody.innerHTML = "";

      if (!cards || cards.length === 0) {
        resultsBody.innerHTML = `
          <tr>
            <td colspan="3" class="table-empty">
              No matching cards were found.
            </td>
          </tr>
        `;
        if (recordsCountEl) {
          recordsCountEl.textContent = "0 Records";
        }
        if (resultsSubtitleEl) {
          resultsSubtitleEl.textContent =
            filterLabel || "No cards match the search.";
        }
        return;
      }

      cards.forEach((card) => {
        const row = document.createElement("tr");
        row.innerHTML = `
          <td>
            <div class="record-meta">
              <div class="record-icon">${CardIcons.creditCard}</div>
              <div>
                <div style="font-weight:600;">
                  ${card.cardholderName || "Unknown"}
                </div>
                <div class="muted">${
                  card.referenceId ? `Ref: ${card.referenceId}` : ""
                }</div>
              </div>
            </div>
          </td>
          <td>${CardUi.formatMaskedPan(card)}</td>
          <td>${CardUi.formatTimestamp(
            card.createdAt || card.createdTime || card.createdDate
          )}</td>
        `;
        resultsBody.appendChild(row);
      });

      if (recordsCountEl) {
        recordsCountEl.textContent = `${cards.length} ${
          cards.length === 1 ? "Record" : "Records"
        }`;
      }
      if (resultsSubtitleEl) {
        resultsSubtitleEl.textContent =
          filterLabel || `Showing ${cards.length} cards`;
      }
    }

    function setActivePanel(panelKey) {
      Object.entries(panels).forEach(([key, panel]) => {
        if (!panel) return;
        if (key === panelKey) {
          panel.classList.remove("panel-hidden");
        } else {
          panel.classList.add("panel-hidden");
        }
      });

      quickActionButtons.forEach((button) => {
        if (button.dataset.panel === panelKey) {
          button.classList.add("active");
          button.setAttribute("aria-pressed", "true");
        } else {
          button.classList.remove("active");
          button.setAttribute("aria-pressed", "false");
        }
      });

      if (panelKey === "search" && state.resultsInitialized) {
        renderResults(state.lastResults, state.lastLabel);
      }

      if (panelKey === "search") {
        window.location.hash = "#search";
      } else {
        window.location.hash = "#add";
      }
    }

    async function refreshTotals() {
      try {
        const payload = await CardApi.listCards();
        const allCards = normalizeCardsResponse(payload);
        state.totalCards = allCards.length;
        setTotals(state.totalCards);

        if (state.lastSearch) {
          await executeSearch(state.lastSearch, { silent: true });
        }
      } catch (error) {
        if (totalCardsEl) {
          totalCardsEl.textContent = "–";
        }
        CardUi.showMessage(
          searchMessage,
          error.message || "Unable to load card data.",
          "error"
        );
      }
    }

    function toDigits(value = "") {
      return value.replace(/\D/g, "");
    }

    function formatPan(value = "") {
      const digits = toDigits(value).slice(0, 16);
      const groups = digits.match(/.{1,4}/g) || [];
      return groups.join(" ");
    }

    async function executeSearch(lastFour, options = {}) {
      const trimmed = (lastFour || "").trim();
      if (trimmed.length !== 4 || /\D/.test(trimmed)) {
        if (!options.silent) {
          CardUi.showMessage(
            searchMessage,
            "Enter exactly four digits to search.",
            "error"
          );
        }
        return;
      }

      try {
        const payload = await CardApi.listCards({ last4: trimmed });
        const cards = normalizeCardsResponse(payload);
        state.lastSearch = trimmed;
        state.lastResults = cards;
        state.lastLabel = cards.length
          ? `Filtered by last 4 digits = ${trimmed}`
          : `No cards match "${trimmed}"`;
        renderResults(cards, state.lastLabel);
        if (!options.silent) {
          CardUi.clearMessage(searchMessage);
        }
      } catch (error) {
        if (!options.silent) {
          CardUi.showMessage(searchMessage, error.message, "error");
        }
      }
    }

    if (addForm) {
      if (panInput) {
        const enforcePanFormatting = () => {
          const formatted = formatPan(panInput.value);
          panInput.value = formatted;
        };

        panInput.addEventListener("input", enforcePanFormatting);
        panInput.addEventListener("blur", enforcePanFormatting);
        panInput.addEventListener("paste", () => {
          requestAnimationFrame(enforcePanFormatting);
        });

        enforcePanFormatting();
      }

      addForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        CardUi.clearMessage(addMessage);

        const nameValue = cardholderInput?.value?.trim();
        const panDigits = toDigits(panInput?.value || "");

        if (!nameValue) {
          CardUi.showMessage(
            addMessage,
            "Cardholder name is required.",
            "error"
          );
          return;
        }

        if (panDigits.length !== 16) {
          CardUi.showMessage(
            addMessage,
            "Please enter a valid 16-digit card number.",
            "error"
          );
          return;
        }

        try {
          await CardApi.addCard({
            cardholderName: nameValue,
            pan: panDigits,
          });
          CardUi.showMessage(
            addMessage,
            "Card added successfully",
            "success"
          );
          addForm.reset();
          if (cardholderInput) cardholderInput.focus();
          state.totalCards += 1;
          setTotals(state.totalCards);
          refreshTotals();
        } catch (error) {
          CardUi.showMessage(addMessage, error.message, "error");
        }
      });
    }

    if (searchForm) {
      searchForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        CardUi.clearMessage(searchMessage);
        await executeSearch(last4Input?.value || "");
      });
    }

    quickActionButtons.forEach((button) => {
      button.addEventListener("click", () => {
        const panelKey = button.dataset.panel;
        setActivePanel(panelKey);
      });
    });

    const initialPanel =
      window.location.hash === "#search" ? "search" : "add";
    setActivePanel(initialPanel);

    refreshTotals();
  },
};

window.CardApi = CardApi;
window.CardUi = CardUi;
window.CardIcons = CardIcons;