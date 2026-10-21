(() => {
  "use strict";

  const API = "api/games";
  const STORAGE_KEY = "square-one.game-id";
  const TERMINAL_STATUSES = new Set([
    "CHECKMATE",
    "RESIGNED",
    "STALEMATE",
    "DRAW_FIFTY_MOVE",
    "DRAW_THREEFOLD_REPETITION",
    "DRAW_INSUFFICIENT_MATERIAL",
    "DRAW_SEVENTY_FIVE_MOVE",
    "DRAW_FIVEFOLD_REPETITION"
  ]);
  const PIECES = {
    WHITE: { KING: "♔", QUEEN: "♕", ROOK: "♖", BISHOP: "♗", KNIGHT: "♘", PAWN: "♙" },
    BLACK: { KING: "♚", QUEEN: "♛", ROOK: "♜", BISHOP: "♝", KNIGHT: "♞", PAWN: "♟" }
  };
  const PROMOTION_LABELS = { q: "Queen", r: "Rook", b: "Bishop", n: "Knight" };

  const elements = {
    appLayout: document.querySelector(".app-layout"),
    board: document.querySelector("#board"),
    feedback: document.querySelector("#feedback"),
    turnPill: document.querySelector("#turn-pill"),
    gameStatus: document.querySelector("#game-status"),
    whiteState: document.querySelector("#white-state"),
    blackState: document.querySelector("#black-state"),
    whiteTurn: document.querySelector("#white-turn"),
    blackTurn: document.querySelector("#black-turn"),
    history: document.querySelector("#move-history"),
    moveCount: document.querySelector("#move-count"),
    claimDraw: document.querySelector("#claim-draw"),
    resign: document.querySelector("#resign"),
    newGame: document.querySelector("#new-game"),
    flipBoard: document.querySelector("#flip-board"),
    promotionDialog: document.querySelector("#promotion-dialog"),
    promotionOptions: document.querySelector("#promotion-options")
  };

  const state = {
    game: null,
    legalMoves: [],
    history: [],
    sanHistory: [],
    selectedSquare: null,
    flipped: false,
    busy: false,
    generation: 0,
    controller: null,
    cancelAnimation: null
  };

  class ApiError extends Error {
    constructor(status, code, message) {
      super(message);
      this.status = status;
      this.code = code;
    }
  }

  async function apiRequest(path, options = {}) {
    let response;
    try {
      response = await fetch(path, {
        ...options,
        headers: {
          Accept: "application/json",
          ...(options.body ? { "Content-Type": "application/json" } : {}),
          ...options.headers
        },
        signal: options.signal || state.controller?.signal
      });
    } catch (error) {
      if (error.name === "AbortError") throw error;
      throw new ApiError(0, "NETWORK_ERROR", "Can't reach the chess server. Check that it is running and try again.");
    }

    let payload;
    try {
      payload = await response.json();
    } catch {
      throw new ApiError(response.status, "INVALID_RESPONSE", "The server returned a response the game could not understand.");
    }
    if (!response.ok) {
      throw new ApiError(
        response.status,
        typeof payload?.code === "string" ? payload.code : "REQUEST_FAILED",
        typeof payload?.message === "string" ? payload.message : "The request could not be completed."
      );
    }
    return payload;
  }

  function errorMessage(error) {
    if (!(error instanceof ApiError)) return "Something went wrong while updating the game. Please try again.";
    if (error.status === 400) return "That request was invalid. Please select a valid move.";
    if (error.status === 404) return "This game is no longer available. Start a new game to continue.";
    if (error.status === 409) return "The game state changed or the game has ended. The board has been refreshed.";
    if (error.status === 422) return "That move is not legal in the current position.";
    return error.message;
  }

  function showFeedback(message = "", kind = "") {
    elements.feedback.textContent = message;
    elements.feedback.className = `feedback${kind ? ` ${kind}` : ""}`;
  }

  function isTerminal() {
    return state.game && TERMINAL_STATUSES.has(state.game.status);
  }

  function fileOf(square) {
    return square.charCodeAt(0) - 97;
  }

  function rankOf(square) {
    return Number(square[1]);
  }

  function squareAt(row, col) {
    return `${String.fromCharCode(97 + col)}${8 - row}`;
  }

  function pieceAt(square) {
    if (!state.game) return null;
    const row = 8 - rankOf(square);
    const col = fileOf(square);
    return state.game.board[row][col];
  }

  function pieceAtSnapshot(game, square) {
    return game.board[8 - rankOf(square)][fileOf(square)];
  }

  function statusLabel(status) {
    const labels = {
      ACTIVE: "In progress",
      CHECK: "Check",
      CHECKMATE: "Checkmate",
      RESIGNED: "Resignation",
      STALEMATE: "Stalemate",
      DRAW_FIFTY_MOVE: "Draw · 50-move rule",
      DRAW_THREEFOLD_REPETITION: "Draw · repetition",
      DRAW_INSUFFICIENT_MATERIAL: "Draw · insufficient material",
      DRAW_SEVENTY_FIVE_MOVE: "Draw · 75-move rule",
      DRAW_FIVEFOLD_REPETITION: "Draw · repetition"
    };
    return labels[status] || "Game status";
  }

  function statusTone(status) {
    if (status === "CHECK") return "check";
    if (status === "CHECKMATE") return "checkmate";
    if (status === "RESIGNED") return "resigned";
    if (status === "STALEMATE") return "stalemate";
    if (status?.startsWith("DRAW_")) return "draw";
    return "active";
  }

  function renderBoard() {
    if (!state.game) return;
    const legalByDestination = new Map();
    if (state.selectedSquare) {
      for (const move of state.legalMoves) {
        if (move.from !== state.selectedSquare) continue;
        const variations = legalByDestination.get(move.to) || [];
        variations.push(move);
        legalByDestination.set(move.to, variations);
      }
    }

    const latestMove = state.history.at(-1);
    const checkSquare = state.game.status === "CHECK" || state.game.status === "CHECKMATE"
      ? findKingSquare(state.game.activeColor)
      : null;
    elements.board.replaceChildren();
    elements.board.setAttribute("aria-busy", String(state.busy));

    for (let displayRow = 0; displayRow < 8; displayRow += 1) {
      const row = state.flipped ? 7 - displayRow : displayRow;
      for (let displayCol = 0; displayCol < 8; displayCol += 1) {
        const col = state.flipped ? 7 - displayCol : displayCol;
        const square = squareAt(row, col);
        const piece = pieceAt(square);
        const button = document.createElement("button");
        button.type = "button";
        button.className = `square ${(row + col) % 2 === 0 ? "light" : "dark"}`;
        button.dataset.square = square;
        button.setAttribute("aria-label", describeSquare(square, piece));
        button.setAttribute("aria-selected", String(square === state.selectedSquare));
        button.tabIndex = (state.selectedSquare || "a8") === square ? 0 : -1;
        button.disabled = state.busy || isTerminal();
        if (square === state.selectedSquare) button.classList.add("selected");
        if (latestMove?.from === square) button.classList.add("last-from");
        if (latestMove?.to === square) button.classList.add("last-to");
        if (checkSquare === square) button.classList.add("in-check");

        const options = legalByDestination.get(square);
        if (options) {
          const marker = document.createElement("span");
          marker.className = piece ? "move-ring" : "move-dot";
          marker.setAttribute("aria-hidden", "true");
          button.append(marker);
        }
        if (piece) {
          const glyph = document.createElement("span");
          glyph.className = `chess-piece ${piece.color === "WHITE" ? "white-piece" : "black-piece"}`;
          glyph.textContent = PIECES[piece.color]?.[piece.type] || piece.symbol;
          glyph.setAttribute("aria-hidden", "true");
          button.append(glyph);
        }
        if ((!state.flipped && row === 7) || (state.flipped && row === 0)) {
          const coordinate = document.createElement("span");
          coordinate.className = "coordinate";
          coordinate.textContent = String.fromCharCode(97 + col);
          coordinate.setAttribute("aria-hidden", "true");
          button.append(coordinate);
        }
        if ((!state.flipped && col === 0) || (state.flipped && col === 7)) {
          const coordinate = document.createElement("span");
          coordinate.className = "coordinate";
          coordinate.style.left = "auto";
          coordinate.style.right = "4px";
          coordinate.style.top = "3px";
          coordinate.style.bottom = "auto";
          coordinate.textContent = String(8 - row);
          coordinate.setAttribute("aria-hidden", "true");
          button.append(coordinate);
        }

        button.addEventListener("click", () => handleSquareClick(square));
        button.addEventListener("keydown", handleBoardKeydown);
        elements.board.append(button);
      }
    }
  }

  function describeSquare(square, piece) {
    if (!piece) return `Empty square ${square}`;
    return `${piece.color === "WHITE" ? "White" : "Black"} ${piece.type.toLowerCase()} on ${square}`;
  }

  function findKingSquare(color) {
    for (let row = 0; row < 8; row += 1) {
      for (let col = 0; col < 8; col += 1) {
        const piece = state.game.board[row][col];
        if (piece?.color === color && piece.type === "KING") return squareAt(row, col);
      }
    }
    return null;
  }

  function renderPanel() {
    if (!state.game) return;
    const { activeColor, status, moveCount } = state.game;
    const ended = isTerminal();
    elements.appLayout.classList.toggle("game-active", !ended);
    const side = activeColor === "WHITE" ? "White" : "Black";
    const winner = activeColor === "WHITE" ? "Black" : "White";
    const turnMessage = status === "CHECKMATE"
      ? `${winner} wins · Checkmate`
      : status === "RESIGNED"
        ? `${side} resigned · ${winner} wins`
        : status === "STALEMATE"
          ? "Draw · Stalemate"
          : status.startsWith("DRAW_")
            ? statusLabel(status)
            : status === "CHECK"
              ? `${side} in check`
              : `${side} to move`;
    const tone = statusTone(status);
    elements.turnPill.textContent = turnMessage;
    elements.turnPill.className = `turn-pill ${tone}`;
    elements.gameStatus.textContent = statusLabel(status);
    elements.gameStatus.className = `status-badge ${tone}`;
    elements.whiteTurn.classList.toggle("active", !ended && activeColor === "WHITE");
    elements.blackTurn.classList.toggle("active", !ended && activeColor === "BLACK");
    elements.whiteTurn.setAttribute("aria-label", !ended && activeColor === "WHITE" ? "White to move" : "Not to move");
    elements.blackTurn.setAttribute("aria-label", !ended && activeColor === "BLACK" ? "Black to move" : "Not to move");
    elements.whiteState.textContent = status === "CHECK" && activeColor === "WHITE" ? "In check" : "";
    elements.blackState.textContent = status === "CHECK" && activeColor === "BLACK" ? "In check" : "";
    elements.moveCount.textContent = `${moveCount} ${moveCount === 1 ? "move" : "moves"}`;
    elements.claimDraw.disabled = state.busy || ended || !state.game.canClaimDraw;
    elements.resign.disabled = state.busy || ended;
    elements.newGame.disabled = state.busy;
    elements.flipBoard.disabled = state.busy;
    renderHistory();
  }

  function renderHistory() {
    elements.history.replaceChildren();
    if (!state.history.length) {
      const empty = document.createElement("p");
      empty.className = "history-empty";
      empty.textContent = "Moves will appear here.";
      elements.history.append(empty);
      return;
    }
    for (let index = 0; index < state.history.length; index += 2) {
      const row = document.createElement("div");
      row.className = "move-row";
      const number = document.createElement("span");
      number.className = "move-number";
      number.textContent = `${Math.floor(index / 2) + 1}.`;
      row.append(number);
      for (let offset = 0; offset < 2; offset += 1) {
        const moveIndex = index + offset;
        const move = state.history[moveIndex];
        const cell = document.createElement("span");
        cell.className = "move-cell";
        if (move) {
          const notation = state.sanHistory[moveIndex]
            || `${move.from}→${move.to}${move.promotion ? `=${move.promotion.toUpperCase()}` : ""}`;
          cell.textContent = notation;
          cell.setAttribute("aria-label", `${offset === 0 ? "White" : "Black"}: ${notation} (${move.from} to ${move.to})`);
          if (moveIndex === state.history.length - 1) cell.classList.add("latest");
        } else {
          cell.classList.add("empty");
          cell.textContent = "—";
          cell.setAttribute("aria-label", "No black move yet");
        }
        row.append(cell);
      }
      elements.history.append(row);
    }
    elements.history.scrollTop = elements.history.scrollHeight;
  }

  function render() {
    if (!state.game) {
      elements.appLayout.classList.remove("game-active");
      elements.board.setAttribute("aria-busy", String(state.busy));
      elements.turnPill.textContent = state.busy ? "Loading game…" : "Game unavailable";
      elements.turnPill.className = `turn-pill ${state.busy ? "loading" : "unavailable"}`;
      elements.gameStatus.textContent = state.busy ? "Connecting" : "Unavailable";
      elements.gameStatus.className = `status-badge ${state.busy ? "loading" : "unavailable"}`;
      elements.claimDraw.disabled = true;
      elements.resign.disabled = true;
      elements.newGame.disabled = state.busy;
      elements.flipBoard.disabled = state.busy;
      return;
    }
    renderBoard();
    renderPanel();
  }

  async function fetchGameData(gameId) {
    const [game, legalMoves, history] = await Promise.all([
      apiRequest(`${API}/${encodeURIComponent(gameId)}`),
      apiRequest(`${API}/${encodeURIComponent(gameId)}/legal-moves`),
      apiRequest(`${API}/${encodeURIComponent(gameId)}/moves`)
    ]);
    return { game, legalMoves, history };
  }

  async function refreshGameData(generation = state.generation) {
    const data = await fetchGameData(state.game.gameId);
    if (generation !== state.generation) return;
    state.game = data.game;
    state.legalMoves = data.legalMoves;
    state.history = data.history;
    state.sanHistory = data.game.sanHistory || [];
    if (state.selectedSquare && !data.legalMoves.some((move) => move.from === state.selectedSquare)) {
      state.selectedSquare = null;
    }
    render();
  }

  function beginGeneration() {
    state.cancelAnimation?.();
    state.controller?.abort();
    state.controller = new AbortController();
    state.generation += 1;
    return state.generation;
  }

  function lockExistingBoard(locked) {
    elements.board.setAttribute("aria-busy", String(locked));
    for (const square of elements.board.querySelectorAll(".square")) {
      square.disabled = locked || isTerminal();
    }
  }

  function clearBoardSelection() {
    state.selectedSquare = null;
    for (const square of elements.board.querySelectorAll(".square.selected")) {
      square.classList.remove("selected");
      square.setAttribute("aria-selected", "false");
    }
    elements.board.querySelectorAll(".move-dot, .move-ring").forEach((marker) => marker.remove());
  }

  function castleRookMove(move, game) {
    const king = pieceAtSnapshot(game, move.from);
    if (king?.type !== "KING" || Math.abs(fileOf(move.to) - fileOf(move.from)) !== 2) return null;
    const rank = move.from[1];
    const kingSide = fileOf(move.to) > fileOf(move.from);
    return {
      from: `${kingSide ? "h" : "a"}${rank}`,
      to: `${kingSide ? "f" : "d"}${rank}`
    };
  }

  function capturedSquares(previousGame, nextGame, move, rookMove) {
    const movingOrigins = new Set([move.from, rookMove?.from].filter(Boolean));
    const captures = new Set();
    for (let row = 0; row < 8; row += 1) {
      for (let col = 0; col < 8; col += 1) {
        const before = previousGame.board[row][col];
        const after = nextGame.board[row][col];
        if (!before) continue;
        const square = squareAt(row, col);
        if (movingOrigins.has(square)) continue;
        if (!after || (after.color !== before.color && square === move.to)) {
          captures.add(square);
        }
      }
    }
    return captures;
  }

  async function animateConfirmedMove(move, previousGame, nextGame, generation) {
    const cleanup = [];
    const animations = [];
    const overlayPieces = [];
    let cleaned = false;

    const finish = (cancel = false) => {
      if (cleaned) return;
      cleaned = true;
      if (cancel) animations.forEach((animation) => animation.cancel());
      cleanup.forEach((restore) => restore());
      overlayPieces.forEach((piece) => piece.remove());
      if (state.cancelAnimation === cancelAnimation) state.cancelAnimation = null;
    };
    const cancelAnimation = () => finish(true);
    state.cancelAnimation = cancelAnimation;

    if (window.matchMedia?.("(prefers-reduced-motion: reduce)").matches) {
      return { finish };
    }

    const rookMove = castleRookMove(move, previousGame);
    const motions = [move, ...(rookMove ? [rookMove] : [])];
    const movingOrigins = new Set(motions.map((motion) => motion.from));
    const captures = capturedSquares(previousGame, nextGame, move, rookMove);
    const duration = 220;

    for (const motion of motions) {
      const sourceSquare = elements.board.querySelector(`[data-square="${motion.from}"]`);
      const targetSquare = elements.board.querySelector(`[data-square="${motion.to}"]`);
      const sourcePiece = sourceSquare?.querySelector(".chess-piece");
      if (!sourceSquare || !targetSquare || !sourcePiece) continue;

      const pieceRect = sourcePiece.getBoundingClientRect();
      const sourceRect = sourceSquare.getBoundingClientRect();
      const targetRect = targetSquare.getBoundingClientRect();
      const width = pieceRect.width || sourceRect.width * 0.8;
      const height = pieceRect.height || sourceRect.height * 0.8;
      const startX = sourceRect.left + sourceRect.width / 2 - width / 2;
      const startY = sourceRect.top + sourceRect.height / 2 - height / 2;
      const endX = targetRect.left + targetRect.width / 2 - width / 2;
      const endY = targetRect.top + targetRect.height / 2 - height / 2;
      const animatedPiece = sourcePiece.cloneNode(true);
      animatedPiece.classList.add("piece-animation");
      animatedPiece.style.width = `${width}px`;
      animatedPiece.style.height = `${height}px`;
      animatedPiece.style.left = "0";
      animatedPiece.style.top = "0";
      animatedPiece.style.transform = `translate3d(${startX}px, ${startY}px, 0)`;
      document.body.append(animatedPiece);
      overlayPieces.push(animatedPiece);

      const oldVisibility = sourcePiece.style.visibility;
      sourcePiece.style.visibility = "hidden";
      cleanup.push(() => { sourcePiece.style.visibility = oldVisibility; });

      if (typeof animatedPiece.animate === "function") {
        animations.push(animatedPiece.animate([
          { transform: `translate3d(${startX}px, ${startY}px, 0)` },
          { transform: `translate3d(${endX}px, ${endY}px, 0)` }
        ], {
          duration,
          easing: "cubic-bezier(0.22, 0.72, 0.24, 1)",
          fill: "forwards"
        }));
      } else {
        animatedPiece.style.transform = `translate3d(${endX}px, ${endY}px, 0)`;
      }
    }

    for (const squareName of captures) {
      if (movingOrigins.has(squareName)) continue;
      const capturedPiece = elements.board
        .querySelector(`[data-square="${squareName}"]`)
        ?.querySelector(".chess-piece");
      if (!capturedPiece) continue;
      if (typeof capturedPiece.animate === "function") {
        animations.push(capturedPiece.animate([{ opacity: 1 }, { opacity: 0 }], {
          duration: 130,
          easing: "ease-out",
          fill: "forwards"
        }));
      } else {
        const oldVisibility = capturedPiece.style.visibility;
        capturedPiece.style.visibility = "hidden";
        cleanup.push(() => { capturedPiece.style.visibility = oldVisibility; });
      }
    }

    try {
      await Promise.all(animations.map((animation) => animation.finished.catch(() => undefined)));
      if (generation !== state.generation) finish(true);
      return { finish };
    } catch (error) {
      finish(true);
      throw error;
    }
  }

  async function initialize() {
    const generation = beginGeneration();
    state.busy = true;
    renderBusy();
    const savedId = sessionStorage.getItem(STORAGE_KEY);
    try {
      if (savedId) {
        try {
          const savedGame = await apiRequest(`${API}/${encodeURIComponent(savedId)}`);
          if (generation !== state.generation) return;
          state.game = savedGame;
        } catch (error) {
          if (generation !== state.generation) return;
          if (!(error instanceof ApiError) || error.status !== 404) throw error;
          sessionStorage.removeItem(STORAGE_KEY);
        }
      }
      if (!state.game) {
        state.game = await apiRequest(API, { method: "POST" });
        if (generation !== state.generation) return;
        sessionStorage.setItem(STORAGE_KEY, state.game.gameId);
      }
      await refreshGameData(generation);
      showFeedback("");
    } catch (error) {
      if (error.name !== "AbortError") showFeedback(errorMessage(error), "error");
    } finally {
      if (generation === state.generation) {
        state.busy = false;
        render();
      }
    }
  }

  function renderBusy() {
    elements.appLayout.classList.remove("game-active");
    elements.board.setAttribute("aria-busy", "true");
    elements.turnPill.textContent = "Loading game…";
    elements.turnPill.className = "turn-pill loading";
    elements.newGame.disabled = true;
    elements.flipBoard.disabled = true;
  }

  function handleBoardKeydown(event) {
    const moves = { ArrowUp: [-1, 0], ArrowDown: [1, 0], ArrowLeft: [0, -1], ArrowRight: [0, 1] };
    const delta = moves[event.key];
    if (!delta) return;
    event.preventDefault();
    const current = event.currentTarget.dataset.square;
    let row = 8 - rankOf(current);
    let col = fileOf(current);
    if (state.flipped) { row = 7 - row; col = 7 - col; }
    const nextRow = row + delta[0];
    const nextCol = col + delta[1];
    if (nextRow < 0 || nextRow > 7 || nextCol < 0 || nextCol > 7) return;
    const nextSquare = state.flipped
      ? squareAt(7 - nextRow, 7 - nextCol)
      : squareAt(nextRow, nextCol);
    elements.board.querySelector(`[data-square="${nextSquare}"]`)?.focus();
  }

  async function handleSquareClick(square) {
    if (state.busy || isTerminal()) return;
    if (state.selectedSquare) {
      if (square === state.selectedSquare) {
        state.selectedSquare = null;
        render();
        return;
      }
      const candidates = state.legalMoves.filter((move) => move.from === state.selectedSquare && move.to === square);
      if (candidates.length) {
        if (candidates.some((move) => move.promotion)) {
          openPromotionDialog(candidates);
        } else {
          await submitMove(candidates[0]);
        }
        return;
      }
    }

    const piece = pieceAt(square);
    if (piece?.color === state.game.activeColor && state.legalMoves.some((move) => move.from === square)) {
      state.selectedSquare = square;
    } else {
      state.selectedSquare = null;
    }
    render();
  }

  function openPromotionDialog(candidates) {
    elements.promotionOptions.replaceChildren();
    for (const move of candidates) {
      if (!move.promotion) continue;
      const button = document.createElement("button");
      button.type = "button";
      button.className = "promotion-option";
      button.setAttribute("aria-label", PROMOTION_LABELS[move.promotion] || `Promote to ${move.promotion}`);
      const piece = document.createElement("span");
      piece.textContent = PIECES[state.game.activeColor]?.[({ q: "QUEEN", r: "ROOK", b: "BISHOP", n: "KNIGHT" })[move.promotion]] || move.promotion.toUpperCase();
      piece.setAttribute("aria-hidden", "true");
      const label = document.createElement("span");
      label.textContent = PROMOTION_LABELS[move.promotion] || move.promotion.toUpperCase();
      button.append(piece, label);
      button.addEventListener("click", () => {
        elements.promotionDialog.close();
        void submitMove(move);
      });
      elements.promotionOptions.append(button);
    }
    elements.promotionDialog.showModal();
    elements.promotionOptions.querySelector("button")?.focus();
  }

  async function submitMove(move) {
    if (state.busy || isTerminal()) return;
    const generation = beginGeneration();
    const previousGame = state.game;
    state.busy = true;
    clearBoardSelection();
    lockExistingBoard(true);
    elements.newGame.disabled = true;
    elements.flipBoard.disabled = true;
    showFeedback("Sending move…");
    try {
      const confirmedGame = await apiRequest(`${API}/${encodeURIComponent(previousGame.gameId)}/moves`, {
        method: "POST",
        body: JSON.stringify({ from: move.from, to: move.to, promotion: move.promotion || null })
      });
      if (generation !== state.generation) return;

      const extraDataPromise = Promise.all([
        apiRequest(`${API}/${encodeURIComponent(confirmedGame.gameId)}`),
        apiRequest(`${API}/${encodeURIComponent(confirmedGame.gameId)}/legal-moves`),
        apiRequest(`${API}/${encodeURIComponent(confirmedGame.gameId)}/moves`)
      ]).then(([game, legalMoves, history]) => ({ game, legalMoves, history }))
        .then((data) => ({ data }), (error) => ({ error }));
      const animation = await animateConfirmedMove(move, previousGame, confirmedGame, generation);
      const extraData = await extraDataPromise;
      if (generation !== state.generation) {
        animation.finish(true);
        return;
      }

      state.game = extraData.data?.game || confirmedGame;
      state.legalMoves = extraData.data?.legalMoves || [];
      state.history = extraData.data?.history || [...state.history, move];
      state.sanHistory = state.game.sanHistory || state.sanHistory;
      animation.finish();
      render();
      if (extraData.error) {
        showFeedback(`Move accepted, but game details could not be refreshed. ${errorMessage(extraData.error)}`, "error");
        return;
      }
      if (isTerminal()) showFeedback(statusLabel(state.game.status), "success");
      else {
        const activePlayer = state.game.activeColor === "WHITE" ? "White" : "Black";
        const moveCue = state.game.status === "CHECK"
          ? `${activePlayer} in check — your move`
          : `${activePlayer} to move — your move`;
        showFeedback(moveCue, "success");
      }
    } catch (error) {
      if (generation !== state.generation) return;
      if (error.name !== "AbortError") {
        showFeedback(errorMessage(error), "error");
        if (error instanceof ApiError &&
            (error.status === 0 || error.status === 409 || error.status >= 500 || error.code === "INVALID_RESPONSE")) {
          try {
            await refreshGameData(generation);
          } catch (refreshError) {
            if (refreshError.name !== "AbortError") showFeedback(`${errorMessage(error)} ${errorMessage(refreshError)}`, "error");
          }
        }
      }
    } finally {
      if (generation === state.generation) {
        state.busy = false;
        lockExistingBoard(false);
        if (state.game) {
          for (const square of elements.board.querySelectorAll(".square")) {
            square.disabled = isTerminal();
          }
          renderPanel();
        }
      }
    }
  }

  async function startNewGame() {
    if (state.busy) return;
    if (state.game?.moveCount > 0 && !window.confirm("Start a new game? You won't be able to return to this game from this tab.")) return;
    const generation = beginGeneration();
    state.busy = true;
    state.game = null;
    state.legalMoves = [];
    state.history = [];
    state.sanHistory = [];
    state.selectedSquare = null;
    showFeedback("Starting a new game…");
    renderBusy();
    try {
      const game = await apiRequest(API, { method: "POST" });
      if (generation !== state.generation) return;
      state.game = game;
      state.sanHistory = game.sanHistory || [];
      sessionStorage.setItem(STORAGE_KEY, game.gameId);
      await refreshGameData(generation);
      showFeedback("");
    } catch (error) {
      if (error.name !== "AbortError") showFeedback(errorMessage(error), "error");
    } finally {
      if (generation === state.generation) {
        state.busy = false;
        render();
      }
    }
  }

  async function endGame(action) {
    if (state.busy || isTerminal()) return;
    if (action === "draw" && !state.game.canClaimDraw) return;
    if (action === "resign" &&
        !window.confirm(`${state.game.activeColor === "WHITE" ? "White" : "Black"} resigns. End this game?`)) return;

    const generation = beginGeneration();
    state.busy = true;
    state.selectedSquare = null;
    lockExistingBoard(true);
    elements.claimDraw.disabled = true;
    elements.resign.disabled = true;
    elements.newGame.disabled = true;
    elements.flipBoard.disabled = true;
    showFeedback(action === "draw" ? "Claiming draw…" : "Resigning…");
    try {
      const game = await apiRequest(`${API}/${encodeURIComponent(state.game.gameId)}/${action}`, {
        method: "POST"
      });
      if (generation !== state.generation) return;
      state.game = game;
      state.sanHistory = game.sanHistory || state.sanHistory;
      state.legalMoves = [];
      render();
      const side = game.activeColor === "WHITE" ? "White" : "Black";
      const opponent = game.activeColor === "WHITE" ? "Black" : "White";
      showFeedback(action === "draw" ? statusLabel(game.status) : `${side} resigned — ${opponent} wins`, "success");
    } catch (error) {
      if (generation !== state.generation) return;
      if (error.name !== "AbortError") {
        const message = action === "draw" && error instanceof ApiError && error.status === 422
          ? "A draw cannot be claimed under the fifty-move or threefold-repetition rules."
          : errorMessage(error);
        showFeedback(message, "error");
      }
    } finally {
      if (generation === state.generation) {
        state.busy = false;
        lockExistingBoard(false);
        render();
      }
    }
  }

  elements.newGame.addEventListener("click", () => void startNewGame());
  elements.claimDraw.addEventListener("click", () => void endGame("draw"));
  elements.resign.addEventListener("click", () => void endGame("resign"));
  elements.flipBoard.addEventListener("click", () => {
    state.flipped = !state.flipped;
    elements.flipBoard.setAttribute("aria-pressed", String(state.flipped));
    render();
  });
  elements.promotionDialog.addEventListener("close", () => {
    if (state.game && !state.busy) render();
  });

  void initialize();
})();
