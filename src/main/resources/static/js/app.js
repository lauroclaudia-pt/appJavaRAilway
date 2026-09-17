/* IPMA Recrutamento — helpers de sessão e acesso à API.
   Autenticação via HTTP Basic: as credenciais confirmadas ficam guardadas
   em sessionStorage (apenas o separador atual, perdidas ao fechar) e são
   anexadas a cada pedido às rotas protegidas. */

const AUTH_KEY = "ipma_auth";

function getAuth() {
  const raw = sessionStorage.getItem(AUTH_KEY);
  return raw ? JSON.parse(raw) : null;
}

function setAuth(email, password) {
  sessionStorage.setItem(AUTH_KEY, JSON.stringify({ email, password }));
}

function clearAuth() {
  sessionStorage.removeItem(AUTH_KEY);
}

function authHeader() {
  const auth = getAuth();
  if (!auth) return {};
  return { Authorization: "Basic " + btoa(auth.email + ":" + auth.password) };
}

/** Pedido autenticado; redireciona para o login se a sessão tiver expirado ou for inválida. */
async function apiFetch(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: { ...(options.headers || {}), ...authHeader() },
  });
  if (res.status === 401) {
    clearAuth();
    window.location.href = "/login.html";
    throw new Error("Sessão expirada.");
  }
  return res;
}

function logout() {
  clearAuth();
  window.location.href = "/index.html";
}

function requireAuth() {
  if (!getAuth()) {
    window.location.href = "/login.html";
  }
}

/** Descarrega um recurso protegido (ex.: PDF da Ata) através de um pedido autenticado,
    já que um link <a href> normal não envia as credenciais HTTP Basic. */
async function downloadAuthenticated(url, filename) {
  const res = await apiFetch(url);
  if (!res.ok) throw new Error("Falha ao obter o ficheiro.");
  const blob = await res.blob();
  const objectUrl = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = objectUrl;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(objectUrl);
}

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  return d.toLocaleDateString("pt-PT", { day: "2-digit", month: "short", year: "numeric" });
}

function roleLabel(role) {
  return {
    ADMIN: "Administrador",
    CDRH: "Chefe de Divisão de RH",
    GESTOR_RH: "Gestor de RH",
    JURI: "Membro do Júri",
    PORTAL: "Candidato",
  }[role] || role;
}

/** user.roles é agora uma lista (um trabalhador pode ter várias responsabilidades). */
function hasRole(user, ...roles) {
  return !!user && Array.isArray(user.roles) && roles.some(r => user.roles.includes(r));
}
