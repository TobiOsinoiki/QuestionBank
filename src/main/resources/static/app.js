const storage = {
  userKey: "qbankUser",
  tokenKey: "qbankToken",
  quizAnswersKey: "qbankQuizAnswers",
  resultKey: "qbankQuizResult",
  otpEmailKey: "qbankOtpEmail",
};

document.addEventListener("DOMContentLoaded", () => {
  const page = document.body.dataset.page;
  bindGlobalActions();

  switch (page) {
    case "register":
      initRegisterPage();
      break;
    case "login":
      initLoginPage();
      break;
    case "dashboard":
      initDashboardPage();
      break;
    case "quiz":
      initQuizPage();
      break;
    case "result":
      initResultPage();
      break;
    case "forgot-password":
      initForgotPasswordPage();
      break;
    case "otp":
      initOtpPage();
      break;
    default:
      break;
  }
});

function bindGlobalActions() {
  document.querySelectorAll("[data-action='logout']").forEach((button) => {
    button.addEventListener("click", logout);
  });
}

function initRegisterPage() {
  const form = document.getElementById("registerForm");
  if (!form) return;

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearFormState(form);

    const fullName = form.fullName.value.trim();
    const email = form.email.value.trim();
    const password = form.password.value.trim();
    let valid = true;

    if (!fullName) {
      setFieldError(form.fullName, "Full name is required.");
      valid = false;
    }

    if (!email) {
      setFieldError(form.email, "Email is required.");
      valid = false;
    } else if (!isValidEmail(email)) {
      setFieldError(form.email, "Enter a valid email address.");
      valid = false;
    }

    if (!password) {
      setFieldError(form.password, "Password is required.");
      valid = false;
    }

    if (!valid) return;

    await withLoading(form, async () => {
      await apiRequest("/api/register", {
        method: "POST",
        body: { fullName, email, password },
      });

      setFormMessage(form, "Registration successful. You can now log in.", "success");
      window.setTimeout(() => {
        window.location.href = "/login.html";
      }, 1200);
    });
  });
}

function initLoginPage() {
  const form = document.getElementById("loginForm");
  if (!form) return;

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearFormState(form);

    const email = form.email.value.trim();
    const password = form.password.value.trim();
    let valid = true;

    if (!email) {
      setFieldError(form.email, "Email is required.");
      valid = false;
    } else if (!isValidEmail(email)) {
      setFieldError(form.email, "Enter a valid email address.");
      valid = false;
    }

    if (!password) {
      setFieldError(form.password, "Password is required.");
      valid = false;
    }

    if (!valid) return;

    await withLoading(form, async () => {
      const data = await apiRequest("/api/login", {
        method: "POST",
        body: { email, password },
      });

      const user = extractUser(data, email);
      const token = data.token || data.accessToken || data.jwt || "";
      saveUserSession(user, token);
      window.location.href = "/dashboard.html";
    });
  });
}

function initDashboardPage() {
  const user = requireUser();
  if (!user) return;

  const target = document.getElementById("welcomeName");
  if (target) {
    target.textContent = user.fullName || user.name || user.username || user.email || "User";
  }
}

async function initQuizPage() {
  const user = requireUser();
  if (!user) return;

  const quizContainer = document.getElementById("quizContainer");
  const submitButton = document.getElementById("submitQuizButton");
  const message = document.getElementById("quizMessage");

  if (!quizContainer || !submitButton) return;

  let questions = [];
  let selectedAnswers = loadJson(storage.quizAnswersKey, {});

  try {
    showLoadingState(quizContainer, "Loading quiz questions...");
    const data = await apiRequest("/api/questions/random", { method: "GET" });
    questions = normalizeQuestions(data);

    if (!questions.length) {
      showEmptyState(quizContainer, "No quiz questions are available right now.");
      submitButton.disabled = true;
      return;
    }

    renderQuestions(quizContainer, questions, selectedAnswers);
    updateQuestionCount(questions.length);

    quizContainer.addEventListener("change", (event) => {
      if (!event.target.matches("input[type='radio'][name^='question-']")) return;
      const questionId = event.target.dataset.questionId;
      selectedAnswers[questionId] = event.target.value;
      localStorage.setItem(storage.quizAnswersKey, JSON.stringify(selectedAnswers));
    });
  } catch (error) {
    showEmptyState(quizContainer, error.message || "Unable to load quiz questions.");
    submitButton.disabled = true;
    setMessage(message, error.message || "Unable to load quiz questions.", "error");
    return;
  }

  submitButton.addEventListener("click", async () => {
    setMessage(message, "");
    const payloadAnswers = questions.map((question) => ({
      questionId: question.id,
      answer: selectedAnswers[question.id] || "",
    }));

    const unanswered = payloadAnswers.filter((item) => !item.answer).length;
    if (unanswered > 0) {
      setMessage(message, `Please answer all questions before submitting. ${unanswered} remaining.`, "error");
      return;
    }

    await withButtonLoading(submitButton, async () => {
      const data = await apiRequest("/api/submit", {
        method: "POST",
        body: { answers: payloadAnswers },
      });

      localStorage.setItem(storage.resultKey, JSON.stringify(normalizeResult(data, questions.length)));
      localStorage.removeItem(storage.quizAnswersKey);
      window.location.href = "/result.html";
    }, (error) => {
      setMessage(message, error.message || "Quiz submission failed.", "error");
    });
  });
}

function initResultPage() {
  const user = requireUser();
  if (!user) return;

  const result = loadJson(storage.resultKey, null);
  if (!result) {
    window.location.href = "/dashboard.html";
    return;
  }

  const scoreEl = document.getElementById("resultScore");
  const statusEl = document.getElementById("resultStatus");
  const messageEl = document.getElementById("resultMessage");
  const summaryEl = document.getElementById("resultSummary");

  if (scoreEl) scoreEl.textContent = result.scoreLabel;
  if (statusEl) {
    statusEl.textContent = result.passed ? "Pass" : "Fail";
    statusEl.classList.add(result.passed ? "success" : "warning");
  }
  if (messageEl) messageEl.textContent = result.message;
  if (summaryEl) {
    summaryEl.innerHTML = `
      <div class="summary-item">
        <strong>${result.correctAnswers}</strong>
        <span>Correct Answers</span>
      </div>
      <div class="summary-item">
        <strong>${result.totalQuestions}</strong>
        <span>Total Questions</span>
      </div>
    `;
  }
}

function initForgotPasswordPage() {
  const form = document.getElementById("forgotPasswordForm");
  if (!form) return;

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearFormState(form);

    const email = form.email.value.trim();
    if (!email) {
      setFieldError(form.email, "Email is required.");
      return;
    }

    if (!isValidEmail(email)) {
      setFieldError(form.email, "Enter a valid email address.");
      return;
    }

    await withLoading(form, async () => {
      await apiRequest("/api/forgot-password", {
        method: "POST",
        body: { email },
      });

      localStorage.setItem(storage.otpEmailKey, email);
      setFormMessage(form, "OTP sent successfully. Redirecting...", "success");
      window.setTimeout(() => {
        window.location.href = "/otp.html";
      }, 1000);
    });
  });
}

function initOtpPage() {
  const form = document.getElementById("otpForm");
  const emailTarget = document.getElementById("otpEmail");
  if (!form) return;

  const email = localStorage.getItem(storage.otpEmailKey) || "";
  if (emailTarget) {
    emailTarget.textContent = email || "your email";
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearFormState(form);

    const otp = form.otp.value.trim();
    if (!otp) {
      setFieldError(form.otp, "OTP code is required.");
      return;
    }

    await withLoading(form, async () => {
      await apiRequest("/api/verify-otp", {
        method: "POST",
        body: { email, otp },
      });

      localStorage.removeItem(storage.otpEmailKey);
      setFormMessage(form, "OTP verified. Redirecting to login...", "success");
      window.setTimeout(() => {
        window.location.href = "/login.html";
      }, 1000);
    });
  });
}

async function apiRequest(url, options = {}) {
  const token = localStorage.getItem(storage.tokenKey);
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    method: options.method || "GET",
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  let data = {};
  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    data = await response.json();
  } else {
    const text = await response.text();
    data = text ? { message: text } : {};
  }

  if (!response.ok) {
    throw new Error(data.message || "Something went wrong. Please try again.");
  }

  return data;
}

function renderQuestions(container, questions, selectedAnswers) {
  container.innerHTML = questions
    .map((question, index) => {
      const optionsHtml = question.options
        .map((option, optionIndex) => {
          const value = option.value;
          const checked = selectedAnswers[question.id] === value ? "checked" : "";
          const inputId = `question-${question.id}-${optionIndex}`;

          return `
            <div class="option-item">
              <input id="${inputId}" type="radio" name="question-${question.id}" value="${escapeHtml(value)}" data-question-id="${escapeHtml(String(question.id))}" ${checked}>
              <label class="option-label" for="${inputId}">
                <span>${escapeHtml(option.label)}</span>
              </label>
            </div>
          `;
        })
        .join("");

      return `
        <article class="question-card">
          <h3>${index + 1}. ${escapeHtml(question.text)}</h3>
          <div class="option-list">${optionsHtml}</div>
        </article>
      `;
    })
    .join("");
}

function normalizeQuestions(data) {
  const list = Array.isArray(data) ? data : data.questions || data.data || [];

  return list.map((item, index) => {
    const optionsSource = item.options || item.choices || item.answers || [];
    const options = optionsSource.map((option, optionIndex) => {
      if (typeof option === "string") {
        return { label: option, value: option };
      }

      return {
        label: option.text || option.label || option.value || `Option ${optionIndex + 1}`,
        value: option.value || option.text || option.label || `option-${optionIndex + 1}`,
      };
    });

    return {
      id: item.id || item.questionId || index + 1,
      text: item.questionText || item.text || item.question || `Question ${index + 1}`,
      options,
    };
  });
}

function normalizeResult(data, totalQuestions) {
  const score = Number(data.score ?? data.totalScore ?? data.correctAnswers ?? 0);
  const resolvedTotal = Number(data.totalQuestions ?? totalQuestions ?? 0);
  const percentage = Number(data.percentage ?? (resolvedTotal ? (score / resolvedTotal) * 100 : 0));
  const passed = typeof data.passed === "boolean" ? data.passed : percentage >= 50;

  return {
    score,
    correctAnswers: Number(data.correctAnswers ?? score),
    totalQuestions: resolvedTotal,
    percentage,
    passed,
    scoreLabel: resolvedTotal ? `${score}/${resolvedTotal}` : `${score}`,
    message: data.message || (passed ? "Great work. You passed the quiz." : "Keep practicing and try again."),
  };
}

function extractUser(data, fallbackEmail) {
  const user = data.user || data.data || {};
  return {
    id: user.id || data.userId || "",
    fullName: user.fullName || user.name || user.username || fallbackEmail,
    email: user.email || fallbackEmail,
  };
}

function saveUserSession(user, token) {
  localStorage.setItem(storage.userKey, JSON.stringify(user));
  if (token) {
    localStorage.setItem(storage.tokenKey, token);
  }
}

function getUser() {
  return loadJson(storage.userKey, null);
}

function requireUser() {
  const user = getUser();
  if (!user) {
    window.location.href = "/login.html";
    return null;
  }
  return user;
}

function logout() {
  localStorage.removeItem(storage.userKey);
  localStorage.removeItem(storage.tokenKey);
  localStorage.removeItem(storage.quizAnswersKey);
  localStorage.removeItem(storage.resultKey);
  window.location.href = "/login.html";
}

function setFieldError(input, message) {
  input.setAttribute("aria-invalid", "true");
  const target = document.querySelector(`[data-error-for='${input.name}']`);
  if (target) target.textContent = message;
}

function clearFormState(form) {
  form.querySelectorAll("[aria-invalid='true']").forEach((element) => {
    element.setAttribute("aria-invalid", "false");
  });

  form.querySelectorAll(".field-error").forEach((element) => {
    element.textContent = "";
  });

  const formMessage = form.querySelector(".form-message");
  if (formMessage) {
    formMessage.textContent = "";
    formMessage.className = "form-message";
  }
}

function setFormMessage(form, message, type = "") {
  const target = form.querySelector(".form-message");
  if (!target) return;
  target.textContent = message;
  target.className = `form-message ${type}`.trim();
}

function setMessage(target, message, type = "") {
  if (!target) return;
  target.textContent = message;
  target.className = `form-message ${type}`.trim();
}

async function withLoading(form, callback) {
  const submitButton = form.querySelector("[type='submit']");
  const defaultLabel = submitButton ? submitButton.dataset.defaultLabel || submitButton.textContent : "";

  try {
    if (submitButton) {
      submitButton.disabled = true;
      submitButton.dataset.defaultLabel = defaultLabel;
      submitButton.textContent = "Please wait...";
    }
    await callback();
  } catch (error) {
    setFormMessage(form, error.message || "Request failed.", "error");
  } finally {
    if (submitButton) {
      submitButton.disabled = false;
      submitButton.textContent = defaultLabel;
    }
  }
}

async function withButtonLoading(button, callback, onError) {
  const defaultLabel = button.dataset.defaultLabel || button.textContent;
  button.dataset.defaultLabel = defaultLabel;

  try {
    button.disabled = true;
    button.textContent = "Submitting...";
    await callback();
  } catch (error) {
    if (typeof onError === "function") {
      onError(error);
    }
  } finally {
    button.disabled = false;
    button.textContent = defaultLabel;
  }
}

function showLoadingState(container, message) {
  container.innerHTML = `<div class="loading-state">${escapeHtml(message)}</div>`;
}

function showEmptyState(container, message) {
  container.innerHTML = `<div class="empty-state">${escapeHtml(message)}</div>`;
}

function updateQuestionCount(count) {
  const target = document.getElementById("questionCount");
  if (target) {
    target.textContent = `${count} question${count === 1 ? "" : "s"}`;
  }
}

function loadJson(key, fallback) {
  try {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : fallback;
  } catch (error) {
    return fallback;
  }
}

function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
