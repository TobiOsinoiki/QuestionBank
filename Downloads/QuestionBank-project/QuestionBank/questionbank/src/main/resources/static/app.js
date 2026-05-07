const storage = {
  userKey:        "qbankUser",
  tokenKey:       "qbankToken",
  quizAnswersKey: "qbankQuizAnswers",
  resultKey:      "qbankQuizResult",
  otpEmailKey:    "qbankOtpEmail",
  otpContextKey:  "qbankOtpContext",
};

// Subject definitions with their topics
const SUBJECTS = {
  JAVA: {
    label: "Java",
    icon: "J",
    topics: ["All Topics","Variables","Data Types","Operators","Control Flow","Loops","OOP","Arrays","Strings","Methods","Exception","Collections","Streams","Lambda","JDBC","Multithreading","Memory","File Handling"]
  },
  CPP: {
    label: "C++",
    icon: "C++",
    topics: ["All Topics","Basics","Pointers","Arrays","Functions","OOP","Templates","STL","Memory Management","Exceptions","File Handling","Namespaces","Preprocessor","Casting","Concurrency"]
  },
  SPRINGBOOT: {
    label: "Spring Boot",
    icon: "SB",
    topics: ["All Topics","Core Concepts","REST API","JPA","Security","Testing","Configuration","Annotations","MVC","Actuator","Data Validation","Caching","Scheduling","Messaging","Exception Handling"]
  },
  REACT: {
    label: "React",
    icon: "Re",
    topics: ["All Topics","JSX","Components","Props","State","Hooks","Context","Routing","Performance","Forms","Testing","Redux","Lifecycle","Suspense","Portals"]
  },
  JAVASCRIPT: {
    label: "JavaScript",
    icon: "JS",
    topics: ["All Topics","Variables","Functions","Arrays","Objects","DOM","Events","Promises","ES6+","Closures","Prototypes","Error Handling","Modules","Iterators","RegEx"]
  },
  HTML: {
    label: "HTML",
    icon: "H",
    topics: ["All Topics","Elements","Attributes","Forms","Semantics","Tables","Media","Meta","Accessibility","Canvas","SVG","Web Storage","Geolocation","Drag and Drop","Web Components"]
  },
  CSS: {
    label: "CSS",
    icon: "CS",
    topics: ["All Topics","Selectors","Box Model","Flexbox","Grid","Animations","Variables","Responsive","Pseudo-classes","Transforms","Transitions","Media Queries","Specificity","Custom Properties","Sass"]
  },
  SQL: {
    label: "SQL",
    icon: "SQL",
    topics: ["All Topics","SELECT","WHERE","JOIN","GROUP BY","Aggregate Functions","Subqueries","INSERT","UPDATE","DELETE","Constraints","Indexes","Transactions","Views","Stored Procedures"]
  }
};

const COUNT_OPTIONS = [10, 20, 30, 40, 50];
const DIFFICULTY_OPTIONS = ["MIXED","EASY","MEDIUM","HARD"];

// ─── Boot ─────────────────────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
  const page = document.body.dataset.page;
  bindGlobalActions();

  switch (page) {
    case "home":            initHomePage();           break;
    case "register":        initRegisterPage();       break;
    case "login":           initLoginPage();          break;
    case "dashboard":       initDashboardPage();      break;
    case "quiz":            initQuizPage();           break;
    case "result":          initResultPage();         break;
    case "history":         initHistoryPage();        break;
    case "leaderboard":     initLeaderboardPage();    break;
    case "profile":         initProfilePage();        break;
    case "forgot-password": initForgotPasswordPage(); break;
    case "otp":             initOtpPage();            break;
    case "reset-password":  initResetPasswordPage();  break;
  }
});

function bindGlobalActions() {
  document.querySelectorAll("[data-action='logout']").forEach(btn =>
    btn.addEventListener("click", logout)
  );
}

// ─── Home ─────────────────────────────────────────────────────────────────────
function initHomePage() {
  const nav  = document.getElementById("homeNav");
  const user = getUser();
  if (!nav) return;
  if (user) {
    nav.innerHTML = `
      <a class="btn btn-secondary" href="/dashboard.html">Dashboard</a>
      <a class="btn btn-secondary" href="/quiz.html">Take Quiz</a>
      <button class="btn btn-danger" type="button" data-action="logout">Logout</button>`;
    bindGlobalActions();
  } else {
    nav.innerHTML = `
      <a class="btn btn-secondary" href="/login.html">Login</a>
      <a class="btn btn-primary"   href="/register.html">Register</a>`;
  }
}

// ─── Register ─────────────────────────────────────────────────────────────────
function initRegisterPage() {
  const form = document.getElementById("registerForm");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearFormState(form);

    const fullName = form.fullName.value.trim();
    const email    = form.email.value.trim();
    const password = form.password.value.trim();
    let valid = true;

    if (!fullName) { setFieldError(form.fullName, "Full name is required."); valid = false; }
    if (!email)    { setFieldError(form.email, "Email is required."); valid = false; }
    else if (!isValidEmail(email)) { setFieldError(form.email, "Enter a valid email address."); valid = false; }
    if (!password) { setFieldError(form.password, "Password is required."); valid = false; }
    if (!valid) return;

    await withLoading(form, async () => {
      await apiRequest("/api/register", { method: "POST", body: { fullName, email, password } });
      localStorage.setItem(storage.otpEmailKey, email);
      localStorage.setItem(storage.otpContextKey, "register");
      setFormMessage(form, "Registration successful! Check your email for OTP.", "success");
      setTimeout(() => { window.location.href = "/otp.html"; }, 1200);
    });
  });
}

// ─── Login ────────────────────────────────────────────────────────────────────
function initLoginPage() {
  const form = document.getElementById("loginForm");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearFormState(form);

    const email    = form.email.value.trim();
    const password = form.password.value.trim();
    let valid = true;

    if (!email) {
      setFieldError(form.email, "Email is required."); valid = false;
    } else if (!isValidEmail(email)) {
      setFieldError(form.email, "Enter a valid email address."); valid = false;
    }
    if (!password) {
      setFieldError(form.password, "Password is required."); valid = false;
    }
    if (!valid) return;

    await withLoading(form, async () => {
      try {
        const data = await apiRequest("/api/login", { method: "POST", body: { email, password } });

        if (data.requiresOtp) {
          localStorage.setItem(storage.otpEmailKey, email);
          localStorage.setItem(storage.otpContextKey, "login");
          setFormMessage(form, "Please verify your email first. Redirecting...", "success");
          setTimeout(() => { window.location.href = "/otp.html"; }, 1000);
          return;
        }

        const user = { id: data.userId, fullName: data.fullName, email: data.email, role: data.role };
        localStorage.setItem(storage.userKey, JSON.stringify(user));
        localStorage.setItem(storage.tokenKey, data.token);

        if (data.role === "ROLE_ADMIN") {
          setFormMessage(form, "Welcome back, admin. Redirecting...", "success");
          setTimeout(() => { window.location.href = "/admin-dashboard.html"; }, 600);
        } else {
          setFormMessage(form, "Login successful. Redirecting...", "success");
          setTimeout(() => { window.location.href = "/dashboard.html"; }, 600);
        }
      } catch (err) {
        setFormMessage(form, err.message || "Invalid email or password.", "error");
      }
    });
  });
}

// ─── Dashboard ────────────────────────────────────────────────────────────────
function initDashboardPage() {
  const user = requireUser();
  if (!user) return;
  const target = document.getElementById("welcomeName");
  if (target) target.textContent = user.fullName || user.email || "User";

  if (user && user.role === "ROLE_ADMIN") {
    const btn = document.getElementById("adminBackBtn");
    if (btn) { btn.style.display = ""; btn.href = "/admin-dashboard.html"; }
  }
}

// ─── Profile ──────────────────────────────────────────────────────────────────
async function initProfilePage() {
  const user = requireUser();
  if (!user) return;
  const form = document.getElementById("profileForm");
  if (!form) return;

  form.fullName.value = user.fullName || "";
  form.email.value    = user.email    || "";

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearFormState(form);

    const fullName    = form.fullName.value.trim();
    const oldPassword = form.oldPassword.value;
    const newPassword = form.newPassword.value;

    if (!fullName) { setFieldError(form.fullName, "Name cannot be empty."); return; }
    if (newPassword && !oldPassword) {
      setFieldError(form.oldPassword, "Enter your current password to set a new one.");
      return;
    }

    await withLoading(form, async () => {
      const body = { fullName };
      if (newPassword) { body.oldPassword = oldPassword; body.newPassword = newPassword; }
      const data = await apiRequest(`/api/profile/${user.id}`, { method: "PUT", body });
      const updated = { ...user, fullName: data.user.fullName };
      localStorage.setItem(storage.userKey, JSON.stringify(updated));
      setFormMessage(form, "Profile updated successfully.", "success");
      form.oldPassword.value = "";
      form.newPassword.value = "";
    });
  });
}

// ─── Quiz ─────────────────────────────────────────────────────────────────────
async function initQuizPage() {
  const user = requireUser();
  if (!user) return;

  const setupScreen  = document.getElementById("setupScreen");
  const quizScreen   = document.getElementById("quizScreen");
  const reviewScreen = document.getElementById("reviewScreen");
  const startBtn     = document.getElementById("startQuizBtn");
  const selectAllBtn = document.getElementById("selectAllBtn");
  const cardsGrid    = document.getElementById("subjectCardsGrid");
  const noMsg        = document.getElementById("noSubjectsMsg");

  // Per-subject: { count, topic, difficulty, selected }
  const selections = {};
  let allSelected = false;

  Object.keys(SUBJECTS).forEach(key => {
    selections[key] = { count: 10, topic: "All Topics", difficulty: "MIXED", selected: false };
  });

  function buildCards() {
    cardsGrid.innerHTML = "";
    const subjectKeys = Object.keys(SUBJECTS);
    if (!subjectKeys.length) { noMsg.style.display = ""; return; }
    noMsg.style.display = "none";

    subjectKeys.forEach(key => {
      const subj = SUBJECTS[key];
      const sel  = selections[key];

      const card = document.createElement("div");
      card.className = "subject-card" + (sel.selected ? " selected" : "");
      card.dataset.key = key;

      card.innerHTML = `
        <div class="subject-card-header">
          <div class="subject-card-icon">${escapeHtml(subj.icon)}</div>
          <div class="subject-card-name">${escapeHtml(subj.label)}</div>
        </div>
        <div class="subject-card-row">
          <div class="subject-card-label">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            Questions
          </div>
          <select class="count-sel" data-key="${key}" onclick="event.stopPropagation()">
            ${COUNT_OPTIONS.map(n => `<option value="${n}" ${n === sel.count ? "selected" : ""}>${n}</option>`).join("")}
          </select>
        </div>
        <div class="subject-card-row">
          <div class="subject-card-label">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            Difficulty
          </div>
          <select class="diff-sel" data-key="${key}" onclick="event.stopPropagation()">
            ${DIFFICULTY_OPTIONS.map(d => `<option value="${d}" ${d === sel.difficulty ? "selected" : ""}>${capitalize(d)}</option>`).join("")}
          </select>
        </div>
        <div class="subject-card-row">
          <div class="subject-card-label">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 6h16M4 10h16M4 14h10"/>
            </svg>
            Topic
          </div>
          <select class="topic-sel" data-key="${key}" onclick="event.stopPropagation()">
            ${subj.topics.map(t => `<option value="${t}" ${t === sel.topic ? "selected" : ""}>${escapeHtml(t)}</option>`).join("")}
          </select>
        </div>
      `;

      card.addEventListener("click", () => {
        sel.selected = !sel.selected;
        card.classList.toggle("selected", sel.selected);
        updateStartBtn();
      });

      card.querySelector(".count-sel").addEventListener("change", e => { sel.count = parseInt(e.target.value); updateDurationHint(); });
      card.querySelector(".diff-sel").addEventListener("change",  e => { sel.difficulty = e.target.value; });
      card.querySelector(".topic-sel").addEventListener("change", e => { sel.topic = e.target.value; });

      cardsGrid.appendChild(card);
    });
  }

  function totalSelectedQuestions() {
    return Object.values(selections)
      .filter(s => s.selected)
      .reduce((sum, s) => sum + s.count, 0);
  }

  function updateDurationHint() {
    const total = totalSelectedQuestions();
    const mode = document.getElementById("selectMode").value;
    const hint = document.getElementById("durationHint");
    
    if (mode === "EXAM") {
      if (hint) hint.textContent = `${total} question${total !== 1 ? "s" : ""} · ${total} min (fixed)`;
    } else {
      if (hint) hint.textContent = `${total} question${total !== 1 ? "s" : ""} · choose duration below`;
    }
  }

  function updateStartBtn() {
    const anySelected = Object.values(selections).some(s => s.selected);
    startBtn.disabled = !anySelected;
    allSelected = Object.values(selections).every(s => s.selected);
    if (selectAllBtn) selectAllBtn.textContent = allSelected ? "Deselect All" : "Select All";
    updateDurationHint();
  }

  selectAllBtn.addEventListener("click", () => {
    allSelected = !allSelected;
    Object.keys(selections).forEach(k => { selections[k].selected = allSelected; });
    buildCards();
    updateStartBtn();
  });

  buildCards();
  updateStartBtn();

  // Mode change — show/hide duration options
  const modeSelect = document.getElementById("selectMode");
  const durationRow = document.getElementById("durationRow");
  
  function updateDurationVisibility() {
    const mode = modeSelect.value;
    if (durationRow) {
      durationRow.style.display = mode === "EXAM" ? "none" : "";
    }
    updateDurationHint();
  }
  
  modeSelect.addEventListener("change", updateDurationVisibility);
  updateDurationVisibility();

  startBtn.addEventListener("click", async () => {
    const mode = document.getElementById("selectMode").value;
    const shuffleQ = document.getElementById("shuffleQ").checked;
    const shuffleO = document.getElementById("shuffleO").checked;

    const selectedSubjects = Object.entries(selections)
      .filter(([, s]) => s.selected)
      .map(([key, s]) => ({ subject: key, count: s.count, topic: s.topic, difficulty: s.difficulty }));

    if (!selectedSubjects.length) return;

    let duration;
    if (mode === "EXAM") {
      // Fixed time: 1 min per question for EXAM
      const totalQ = selectedSubjects.reduce((sum, s) => sum + s.count, 0);
      duration = totalQ;
    } else {
      // Custom duration for PRACTICE/STUDY
      duration = parseInt(document.getElementById("examDuration").value) || 30;
    }

    setupScreen.style.display = "none";
    quizScreen.style.display = "";

    await runQuiz(user, selectedSubjects, mode, duration, shuffleQ, shuffleO, quizScreen, reviewScreen);
  });
}

// ─── Quiz runner ──────────────────────────────────────────────────────────────
async function runQuiz(user, selectedSubjects, mode, durationMinutes, shuffleQ, shuffleO, quizScreen, reviewScreen) {
  const slideContainer = document.getElementById("slideContainer");
  const submitButton = document.getElementById("submitQuizButton");
  const endTestBtn = document.getElementById("endTestBtn");
  const prevBtn = document.getElementById("prevBtn");
  const nextBtn = document.getElementById("nextBtn");
  const message = document.getElementById("quizMessage");
  const modeLabel = document.getElementById("quizModeLabel");
  const timerChip = document.getElementById("timerChip");
  const progressFill = document.getElementById("progressFill");
  const progressLabel = document.getElementById("progressLabel");
  const subjectTabBar = document.getElementById("quizSubjectTabBar");
  const questionNavGrid = document.getElementById("questionNavGrid");

  if (modeLabel) modeLabel.textContent = `${capitalize(mode)} Mode`;

  let questions = [];
  let selectedAnswers = {};
  let currentIndex = 0;
  let timerInterval = null;

  // Fetch questions per subject+topic+difficulty
  try {
    slideContainer.innerHTML = `<div class="loading-state">Loading quiz questions...</div>`;

    const fetchPromises = selectedSubjects.map(({ subject, count, topic, difficulty }) => {
      const diff = difficulty || "MIXED";
      const topicParam = (topic && topic !== "All Topics")
        ? `&topic=${encodeURIComponent(topic)}` : "";
      return apiRequest(
        `/api/questions/random?difficulty=${diff}&count=${count}&subject=${encodeURIComponent(subject)}${topicParam}`,
        { method: "GET" }
      );
    });

    const results = await Promise.all(fetchPromises);

    // Tag each question with its subject for grouping
    results.forEach((batch, idx) => {
      batch.forEach(q => { q._subject = selectedSubjects[idx].subject; });
    });

    questions = results.flat();

    if (shuffleQ) {
      for (let i = questions.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [questions[i], questions[j]] = [questions[j], questions[i]];
      }
    }

    if (shuffleO) questions = questions.map(q => shuffleOptions(q));

    if (!questions.length) {
      slideContainer.innerHTML = `<div class="empty-state">No questions available for selected subjects/topics.</div>`;
      if (submitButton) submitButton.style.display = "none";
      return;
    }

    updateQuestionCount(questions.length);
    buildSubjectTabs();
    buildQuestionNav();
    renderSlide(currentIndex);

    slideContainer.addEventListener("change", (e) => {
      if (!e.target.matches("input[type='radio']")) return;
      const qId = e.target.dataset.questionId;
      const chosen = e.target.value;
      selectedAnswers[qId] = chosen;
      updateNavDot(qId);
      if (mode === "STUDY") {
        const q = questions.find(q => String(q.id) === String(qId));
        if (q) showInstantFeedback(qId, chosen, q.correctAnswer, q);
      }
    });

    // Timer
    if (mode !== "STUDY" && durationMinutes > 0) {
      const totalSeconds = durationMinutes * 60;
      let remaining = totalSeconds;
      if (timerChip) { timerChip.style.display = ""; timerChip.textContent = formatTime(totalSeconds); }
      timerInterval = setInterval(() => {
        remaining--;
        if (timerChip) timerChip.textContent = formatTime(remaining);
        if (remaining <= 300 && timerChip) timerChip.classList.add("timer-warn");
        if (remaining <= 0) {
          clearInterval(timerInterval);
          setMessage(message, "Time is up! Submitting automatically.", "error");
          doSubmit();
        }
      }, 1000);
    } else {
      if (timerChip) timerChip.style.display = "none";
    }

  } catch (err) {
    slideContainer.innerHTML = `<div class="empty-state">${escapeHtml(err.message || "Failed to load questions.")}</div>`;
    return;
  }

  // ── Subject tab bar (top of quiz) ──
  function buildSubjectTabs() {
    if (!subjectTabBar) return;
    const subjKeys = [...new Set(questions.map(q => q._subject || q.subject || ""))].filter(Boolean);
    if (subjKeys.length <= 1) { subjectTabBar.style.display = "none"; return; }

    subjectTabBar.innerHTML = subjKeys.map(k => {
      const lbl = (SUBJECTS[k] || {}).label || k;
      return `<button class="quiz-subject-tab" data-subject="${escapeHtml(k)}">${escapeHtml(lbl)}</button>`;
    }).join("");

    subjectTabBar.querySelectorAll(".quiz-subject-tab").forEach(btn => {
      btn.addEventListener("click", () => {
        const subj = btn.dataset.subject;
        const idx = questions.findIndex(q => (q._subject || q.subject) === subj);
        if (idx >= 0) goTo(idx);
      });
    });
  }

  function updateSubjectTabHighlight() {
    if (!subjectTabBar) return;
    const curSubj = questions[currentIndex]?._subject || questions[currentIndex]?.subject || "";
    subjectTabBar.querySelectorAll(".quiz-subject-tab").forEach(btn => {
      btn.classList.toggle("active", btn.dataset.subject === curSubj);
    });
  }

  // ── Question number nav (sidebar) ──
  function buildQuestionNav() {
    if (!questionNavGrid) return;
    questionNavGrid.innerHTML = questions.map((q, i) => `
      <button class="q-nav-dot" data-idx="${i}" title="Q${i+1}">${i+1}</button>
    `).join("");

    questionNavGrid.querySelectorAll(".q-nav-dot").forEach(btn => {
      btn.addEventListener("click", () => goTo(parseInt(btn.dataset.idx)));
    });
  }

  function updateNavDot(questionId) {
    const idx = questions.findIndex(q => String(q.id) === String(questionId));
    if (idx < 0 || !questionNavGrid) return;
    const dot = questionNavGrid.querySelector(`[data-idx="${idx}"]`);
    if (dot) dot.classList.add("answered");
  }

  function updateNavHighlight() {
    if (!questionNavGrid) return;
    questionNavGrid.querySelectorAll(".q-nav-dot").forEach((dot, i) => {
      dot.classList.toggle("current", i === currentIndex);
    });
  }

  function goTo(index) {
    if (index < 0 || index >= questions.length) return;
    currentIndex = index;
    renderSlide(currentIndex);
  }

  // ── Render question slide ──
  function renderSlide(index) {
    const q = questions[index];
    if (!q) return;
    const saved = selectedAnswers[String(q.id)] || null;

    slideContainer.innerHTML = `
      <article class="question-card" data-card-id="${q.id}" style="animation:slideUp 0.25s ease both">
        <div class="question-meta">
          <span class="q-subject-badge">${escapeHtml((SUBJECTS[q._subject || q.subject] || {}).label || q.subject || "")}</span>
          <span class="q-topic-badge">${escapeHtml(q.topic || "")}</span>
          <span class="q-diff-badge diff-${(q.difficulty || "").toLowerCase()}">${escapeHtml(q.difficulty || "")}</span>
        </div>
        <h3 class="question-text">${index + 1}. ${escapeHtml(q.text)}</h3>
        <div class="option-list">
          ${q.options.map((opt, oi) => {
            const inputId = `q-${q.id}-${oi}`;
            return `<div class="option-item">
              <input id="${inputId}" type="radio" name="question-${q.id}"
                     value="${escapeHtml(opt.value)}"
                     data-question-id="${escapeHtml(String(q.id))}"
                     ${saved === opt.value ? "checked" : ""}>
              <label class="option-label" for="${inputId}">
                <span class="option-letter">${opt.value}</span>
                <span>${escapeHtml(opt.label)}</span>
              </label>
            </div>`;
          }).join("")}
        </div>
        ${mode === "STUDY" && saved ? buildInstantFeedbackHTML(String(q.id), saved, q.correctAnswer) : ""}
      </article>`;

    const pct = Math.round(((index + 1) / questions.length) * 100);
    if (progressFill) progressFill.style.width = `${pct}%`;
    if (progressLabel) progressLabel.textContent = `Question ${index + 1} of ${questions.length}`;

    if (prevBtn) prevBtn.disabled = index === 0;
    const isLast = index === questions.length - 1;
    if (nextBtn) nextBtn.style.display = isLast ? "none" : "";
    if (submitButton) submitButton.style.display = isLast ? "" : "none";

    updateNavHighlight();
    updateSubjectTabHighlight();

    // Scroll the nav dot into view
    if (questionNavGrid) {
      const dot = questionNavGrid.querySelector(`[data-idx="${index}"]`);
      if (dot) dot.scrollIntoView({ block: "nearest" });
    }
  }

  prevBtn?.addEventListener("click", () => { if (currentIndex > 0) goTo(currentIndex - 1); });
  nextBtn?.addEventListener("click", () => { if (currentIndex < questions.length - 1) goTo(currentIndex + 1); });

  async function doSubmit() {
    if (timerInterval) clearInterval(timerInterval);
    if (submitButton) submitButton.disabled = true;
    if (endTestBtn) endTestBtn.disabled = true;

    const payloadAnswers = questions.map(q => ({
      questionId: q.id,
      answer: selectedAnswers[String(q.id)] || "",
    }));

    const recordUserId = (mode === "STUDY") ? null : (user.id || null);

    try {
      const data = await apiRequest("/api/submit", {
        method: "POST",
        body: {
          answers: payloadAnswers,
          mode,
          difficulty: "MIXED",
          userId: recordUserId,
        },
      });

      localStorage.setItem(storage.resultKey, JSON.stringify(normalizeResult(data, questions.length)));

      if (mode === "EXAM") {
        window.location.href = "/result.html";
      } else {
        quizScreen.style.display = "none";
        reviewScreen.style.display = "";
        renderReview(data.review || [], questions);
      }
    } catch (err) {
      setMessage(message, err.message || "Submission failed.", "error");
      if (submitButton) submitButton.disabled = false;
      if (endTestBtn) endTestBtn.disabled = false;
    }
  }

  submitButton?.addEventListener("click", async () => {
    setMessage(message, "");
    const unanswered = questions.filter(q => !selectedAnswers[String(q.id)]).length;
    if (unanswered > 0 && mode !== "STUDY") {
      if (submitButton.dataset.confirm !== "1") {
        submitButton.dataset.confirm = "1";
        setMessage(message, `${unanswered} question(s) unanswered. Click Submit again to confirm.`, "error");
        return;
      }
    }
    submitButton.dataset.confirm = "";
    await doSubmit();
  });

  endTestBtn?.addEventListener("click", async () => {
    const answered = questions.filter(q => selectedAnswers[String(q.id)]).length;
    if (!confirm(`End test now? You have answered ${answered} of ${questions.length} questions.`)) return;
    await doSubmit();
  });

  // ── Shuffle options helper ──
  function shuffleOptions(q) {
    const options = [
      { label: q.options[0].label, value: q.options[0].value, origVal: "A" },
      { label: q.options[1].label, value: q.options[1].value, origVal: "B" },
      { label: q.options[2].label, value: q.options[2].value, origVal: "C" },
      { label: q.options[3].label, value: q.options[3].value, origVal: "D" },
    ];
    for (let i = options.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [options[i], options[j]] = [options[j], options[i]];
    }
    const labels = ["A","B","C","D"];
    const newCorrectIdx = options.findIndex(o => o.origVal === q.correctAnswer);
    return {
      ...q,
      correctAnswer: labels[newCorrectIdx],
      options: options.map((o, i) => ({ label: o.label, value: labels[i] })),
      explanationA: q[`explanation${options[0].origVal}`],
      explanationB: q[`explanation${options[1].origVal}`],
      explanationC: q[`explanation${options[2].origVal}`],
      explanationD: q[`explanation${options[3].origVal}`],
    };
  }
}

function buildInstantFeedbackHTML(questionId, chosen, correctAnswer) {
  const isCorrect = chosen === correctAnswer;
  return `<p class="instant-feedback" style="color:${isCorrect ? "var(--success)" : "var(--danger)"};font-weight:600;margin-top:8px">
    ${isCorrect ? "✓ Correct!" : `✗ Incorrect. Correct answer: ${correctAnswer}`}
  </p>`;
}

function showInstantFeedback(questionId, chosen, correctAnswer, question) {
  const card = document.querySelector(`[data-card-id="${questionId}"]`);
  if (!card) return;
  let fb = card.querySelector(".instant-feedback");
  if (!fb) { fb = document.createElement("div"); fb.className = "instant-feedback"; card.appendChild(fb); }
  const isCorrect = chosen === correctAnswer;
  const chosenExp = question ? question[`explanation${chosen}`] : null;
  const correctExp = question ? question[`explanation${correctAnswer}`] : null;

  if (isCorrect) {
    fb.innerHTML = `<p style="color:var(--success);font-weight:600">✓ Correct!</p>
      ${correctExp ? `<p style="font-size:0.88rem;color:var(--muted);margin-top:4px">${escapeHtml(correctExp)}</p>` : ""}`;
  } else {
    fb.innerHTML = `<p style="color:var(--danger);font-weight:600">✗ Incorrect. Correct answer: ${correctAnswer}</p>
      ${chosenExp ? `<div style="padding:8px;border-left:3px solid var(--danger);background:rgba(220,38,38,0.06);margin-top:6px;font-size:0.85rem">${escapeHtml(chosenExp)}</div>` : ""}
      ${correctExp ? `<div style="padding:8px;border-left:3px solid var(--success);background:rgba(21,128,61,0.06);margin-top:6px;font-size:0.85rem">${escapeHtml(correctExp)}</div>` : ""}`;
  }
}

function renderReview(reviewData, questions) {
  const container = document.getElementById("reviewContainer");
  if (!container) return;

  const correct = reviewData.filter(r => r.isCorrect).length;
  const total = reviewData.length;

  container.innerHTML = `
    <p style="font-size:1.1rem;font-weight:700;margin-bottom:16px">
      You got <span style="color:var(--primary)">${correct}/${total}</span> correct.
    </p>
  `;

  reviewData.forEach((item, index) => {
    const card = document.createElement("article");
    card.className = `question-card ${item.isCorrect ? "review-correct" : "review-wrong"}`;
    const submitted = item.submitted || "Not answered";
    const correctAns = item.correctAnswer;
    card.innerHTML = `
      <h3>${index + 1}. ${escapeHtml(item.questionText)}</h3>
      <p style="margin:8px 0 4px;font-size:0.9rem;color:${item.isCorrect ? "var(--success)" : "var(--danger)"}">
        ${item.isCorrect ? "✓ Correct" : "✗ Incorrect"}
      </p>
      <p style="font-size:0.9rem;color:var(--muted);margin-bottom:10px">
        Your answer: <strong>${escapeHtml(submitted)}</strong> &nbsp;|&nbsp;
        Correct answer: <strong>${escapeHtml(correctAns)}</strong>
      </p>
      ${!item.isCorrect ? `
        <div style="padding:10px;margin-bottom:8px;border-left:4px solid var(--danger);background:rgba(220,38,38,0.06)">
          ${escapeHtml(item.submittedExplanation || "No explanation available.")}
        </div>` : ""}
      <div style="padding:10px;border-left:4px solid var(--success);background:rgba(21,128,61,0.06)">
        ${escapeHtml(item.correctExplanation || "No explanation provided.")}
      </div>
    `;
    container.appendChild(card);
  });
}

// ─── Rest of the functions (Result, History, Leaderboard, etc.) ───────────────
function initResultPage() {
  const user = requireUser();
  if (!user) return;
  const result = loadJson(storage.resultKey, null);
  if (!result) { window.location.href = "/dashboard.html"; return; }

  const scoreEl = document.getElementById("resultScore");
  const statusEl = document.getElementById("resultStatus");
  const messageEl = document.getElementById("resultMessage");
  const summaryEl = document.getElementById("resultSummary");

  if (scoreEl) scoreEl.textContent = result.scoreLabel;
  if (statusEl) { statusEl.textContent = result.passed ? "Pass" : "Fail"; statusEl.classList.add(result.passed ? "success" : "warning"); }
  if (messageEl) messageEl.textContent = result.message;
  if (summaryEl) summaryEl.innerHTML = `
    <div class="summary-item"><strong>${result.correctAnswers}</strong><span>Correct Answers</span></div>
    <div class="summary-item"><strong>${result.totalQuestions}</strong><span>Total Questions</span></div>`;
}

async function initHistoryPage() {
  const user = requireUser();
  if (!user) return;
  const container = document.getElementById("historyContainer");
  if (!container) return;
  try {
    const data = await apiRequest(`/api/history/${user.id}`, { method: "GET" });
    if (!data.length) { showEmptyState(container, "No quiz attempts yet. Take a quiz to see your history here."); return; }
    container.innerHTML = "";
    data.forEach(attempt => {
      const date = new Date(attempt.attemptedAt).toLocaleString();
      const card = document.createElement("div");
      card.className = "history-card";
      card.innerHTML = `
        <div class="history-row">
          <div>
            <span class="eyebrow">${attempt.mode} · ${attempt.difficulty}</span>
            <p style="margin:6px 0 2px;font-weight:700">${attempt.correctAnswers}/${attempt.questionCount} correct
              <span style="color:var(--muted);font-weight:400">(${attempt.percentage.toFixed(1)}%)</span></p>
            <p style="margin:0;font-size:0.85rem;color:var(--muted)">${date}</p>
          </div>
          <div style="text-align:right">
            <span class="status-chip ${attempt.passed ? "success" : "warning"}">${attempt.passed ? "Pass" : "Fail"}</span>
            <p style="margin:6px 0 0;font-size:0.85rem;color:var(--muted)">Score: ${attempt.weightedScore}</p>
          </div>
        </div>`;
      container.appendChild(card);
    });
  } catch (err) {
    showEmptyState(container, err.message || "Failed to load history.");
  }
}

async function initLeaderboardPage() {
  const user = requireUser();
  if (!user) return;
  const container = document.getElementById("leaderboardContainer");
  if (!container) return;
  try {
    const data = await apiRequest("/api/leaderboard", { method: "GET" });
    if (!data.length) { showEmptyState(container, "No data yet. Be the first to complete a quiz!"); return; }
    const table = document.createElement("table");
    table.className = "leaderboard-table";
    table.innerHTML = `
      <thead><tr><th>#</th><th>Name</th><th>Score</th><th>Best</th><th>Attempts</th><th>Avg %</th></tr></thead>
      <tbody>${data.map((row, i) => `
        <tr class="${row.userId == user.id ? "leaderboard-self" : ""}">
          <td>${i + 1}</td>
          <td>${escapeHtml(String(row.fullName))}${row.userId == user.id ? " (you)" : ""}</td>
          <td>${row.leaderboardScore}</td>
          <td>${row.bestWeighted}</td>
          <td>${row.totalAttempts}</td>
          <td>${row.avgPercentage}%</td>
        </tr>`).join("")}</tbody>`;
    container.innerHTML = "";
    container.appendChild(table);
  } catch (err) {
    showEmptyState(container, err.message || "Failed to load leaderboard.");
  }
}

function initForgotPasswordPage() {
  const form = document.getElementById("forgotPasswordForm");
  if (!form) return;
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearFormState(form);
    const email = form.email.value.trim();
    if (!email) { setFieldError(form.email, "Email is required."); return; }
    if (!isValidEmail(email)) { setFieldError(form.email, "Enter a valid email address."); return; }
    await withLoading(form, async () => {
      await apiRequest("/api/forgot-password", { method: "POST", body: { email } });
      localStorage.setItem(storage.otpEmailKey, email);
      localStorage.setItem(storage.otpContextKey, "forgot-password");
      setFormMessage(form, "OTP sent successfully. Redirecting...", "success");
      setTimeout(() => { window.location.href = "/otp.html"; }, 1000);
    });
  });
}

function initOtpPage() {
  const form = document.getElementById("otpForm");
  const emailTarget = document.getElementById("otpEmail");
  const resendBtn = document.getElementById("resendOtpBtn");
  const resendTimer = document.getElementById("resendTimer");
  const timerCount = document.getElementById("timerCount");

  if (!form) return;

  const email = localStorage.getItem(storage.otpEmailKey) || "";
  if (emailTarget) emailTarget.textContent = email || "your email";

  let resendCooldown = 0;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearFormState(form);
    const otp = form.otp.value.trim();
    if (!otp) { setFieldError(form.otp, "OTP code is required."); return; }

    await withLoading(form, async () => {
      await apiRequest("/api/verify-otp", { method: "POST", body: { email, otp } });
      const context = localStorage.getItem(storage.otpContextKey) || "register";
      localStorage.removeItem(storage.otpEmailKey);
      localStorage.removeItem(storage.otpContextKey);
      setFormMessage(form, "OTP verified. Redirecting...", "success");
      setTimeout(() => {
        if (context === "forgot-password") {
          localStorage.setItem(storage.otpEmailKey, email);
          window.location.href = "/reset-password.html";
        } else {
          window.location.href = "/login.html";
        }
      }, 1000);
    });
  });

  if (resendBtn) {
    resendBtn.addEventListener("click", async () => {
      if (resendCooldown > 0) return;
      await withButtonLoading(resendBtn, async () => {
        await apiRequest("/api/resend-otp", { method: "POST", body: { email } });
        setFormMessage(form, "New OTP sent successfully!", "success");
        startResendCooldown();
      });
    });
  }

  function startResendCooldown() {
    resendCooldown = 60;
    if (resendBtn) resendBtn.style.display = "none";
    if (resendTimer) resendTimer.style.display = "inline-block";
    const interval = setInterval(() => {
      resendCooldown--;
      if (timerCount) timerCount.textContent = resendCooldown;
      if (resendCooldown <= 0) {
        clearInterval(interval);
        if (resendBtn) resendBtn.style.display = "";
        if (resendTimer) resendTimer.style.display = "none";
      }
    }, 1000);
  }
}

function initResetPasswordPage() {
  const form = document.getElementById("resetPasswordForm");
  if (!form) return;
  const email = localStorage.getItem(storage.otpEmailKey) || "";
  if (!email) { window.location.href = "/forgot-password.html"; return; }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearFormState(form);
    const newPassword = form.newPassword.value;
    const confirmPassword = form.confirmPassword.value;
    if (!newPassword) { setFieldError(form.newPassword, "Password is required."); return; }
    if (newPassword !== confirmPassword) { setFieldError(form.confirmPassword, "Passwords do not match."); return; }
    await withLoading(form, async () => {
      await apiRequest("/api/reset-password", { method: "POST", body: { email, newPassword } });
      localStorage.removeItem(storage.otpEmailKey);
      setFormMessage(form, "Password reset successfully. Redirecting to login...", "success");
      setTimeout(() => { window.location.href = "/login.html"; }, 1200);
    });
  });
}

// ─── API ──────────────────────────────────────────────────────────────────────
async function apiRequest(url, options = {}) {
  const token = localStorage.getItem(storage.tokenKey);
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(url, {
    method: options.method || "GET",
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  let data = {};
  const ct = response.headers.get("content-type") || "";
  if (ct.includes("application/json")) {
    data = await response.json();
  } else {
    const text = await response.text();
    data = text ? { message: text } : {};
  }

  if (!response.ok) throw new Error(data.message || "Something went wrong. Please try again.");
  return data;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function normalizeResult(data, totalQuestions) {
  const score = Number(data.correctAnswers ?? 0);
  const resolvedTotal = Number(data.totalQuestions ?? totalQuestions ?? 0);
  const percentage = Number(data.percentage ?? 0);
  const passed = typeof data.passed === "boolean" ? data.passed : percentage >= 50;
  return {
    score, correctAnswers: score, totalQuestions: resolvedTotal, percentage, passed,
    scoreLabel: resolvedTotal ? `${score}/${resolvedTotal}` : `${score}`,
    message: data.message || (passed ? "Great work. You passed the quiz." : "Keep practicing and try again."),
  };
}

function getUser() { return loadJson(storage.userKey, null); }
function requireUser() {
  const user = getUser();
  if (!user) { window.location.href = "/login.html"; return null; }
  return user;
}
function logout() {
  [storage.userKey, storage.tokenKey, storage.quizAnswersKey, storage.resultKey]
    .forEach(k => localStorage.removeItem(k));
  window.location.href = "/login.html";
}

function setFieldError(input, msg) {
  input.setAttribute("aria-invalid", "true");
  const t = document.querySelector(`[data-error-for='${input.name}']`);
  if (t) t.textContent = msg;
}
function clearFormState(form) {
  form.querySelectorAll("[aria-invalid='true']").forEach(el => el.setAttribute("aria-invalid", "false"));
  form.querySelectorAll(".field-error").forEach(el => el.textContent = "");
  const fm = form.querySelector(".form-message");
  if (fm) { fm.textContent = ""; fm.className = "form-message"; }
}
function setFormMessage(form, msg, type = "") {
  const t = form.querySelector(".form-message");
  if (!t) return;
  t.textContent = msg;
  t.className = `form-message ${type}`.trim();
}
function setMessage(target, msg, type = "") {
  if (!target) return;
  target.textContent = msg;
  target.className = `form-message ${type}`.trim();
}
function showEmptyState(c, msg) { c.innerHTML = `<div class="empty-state">${escapeHtml(msg)}</div>`; }
function updateQuestionCount(n) {
  const t = document.getElementById("questionCount");
  if (t) t.textContent = `${n} question${n === 1 ? "" : "s"}`;
}
function formatTime(s) {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = s % 60;
  if (h > 0) return `${h}:${m.toString().padStart(2,"0")}:${sec.toString().padStart(2,"0")}`;
  return `${m.toString().padStart(2,"0")}:${sec.toString().padStart(2,"0")}`;
}
function capitalize(s) { return s.charAt(0).toUpperCase() + s.slice(1).toLowerCase(); }

async function withLoading(form, callback) {
  const btn = form.querySelector("[type='submit']");
  const label = btn ? btn.dataset.defaultLabel || btn.textContent : "";
  try {
    if (btn) { btn.disabled = true; btn.dataset.defaultLabel = label; btn.textContent = "Please wait..."; }
    await callback();
  } catch (err) {
    setFormMessage(form, err.message || "Request failed.", "error");
  } finally {
    if (btn) { btn.disabled = false; btn.textContent = label; }
  }
}
async function withButtonLoading(button, callback, onError) {
  const label = button.dataset.defaultLabel || button.textContent;
  button.dataset.defaultLabel = label;
  try {
    button.disabled = true;
    button.textContent = "Sending...";
    await callback();
  } catch (err) {
    if (typeof onError === "function") onError(err);
  } finally {
    button.disabled = false;
    button.textContent = label;
  }
}
function loadJson(key, fallback) {
  try { const v = localStorage.getItem(key); return v ? JSON.parse(v) : fallback; }
  catch { return fallback; }
}
function isValidEmail(v) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v); }
function escapeHtml(v) {
  return String(v ?? "")
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

// ─── Password toggle ──────────────────────────────────────────────────────────
(function initPasswordToggles() {
  const EYE_OPEN = `<svg xmlns="[w3.org](http://www.w3.org/2000/svg)" width="20" height="20" viewBox="0 0 24 24"
       fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>
  </svg>`;
  const EYE_OFF = `<svg xmlns="[w3.org](http://www.w3.org/2000/svg)" width="20" height="20" viewBox="0 0 24 24"
       fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/>
    <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/>
    <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"/>
    <line x1="2" y1="2" x2="22" y2="22"/>
  </svg>`;

  function attachToggle(input) {
    if (input.dataset.toggleAttached === "true") return;
    input.dataset.toggleAttached = "true";
    const wrapper = document.createElement("div");
    wrapper.className = "password-wrapper";
    input.parentNode.insertBefore(wrapper, input);
    wrapper.appendChild(input);
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "password-toggle";
    btn.setAttribute("aria-label", "Show password");
    btn.setAttribute("tabindex", "-1");
    btn.innerHTML = EYE_OPEN;
    wrapper.appendChild(btn);
    btn.addEventListener("click", () => {
      const showing = input.type === "text";
      input.type = showing ? "password" : "text";
      btn.setAttribute("aria-label", showing ? "Show password" : "Hide password");
      btn.innerHTML = showing ? EYE_OPEN : EYE_OFF;
    });
  }

  function init() { document.querySelectorAll('input[type="password"]').forEach(attachToggle); }
  if (document.readyState === "loading") { document.addEventListener("DOMContentLoaded", init); }
  else { init(); }
})();
