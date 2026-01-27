document.addEventListener('DOMContentLoaded', () => {
  const pagination = document.getElementById('pagination');
  if (!pagination) return;

  pagination.addEventListener('click', e => {
    if (e.target.tagName !== 'BUTTON') return;
    const page = e.target.dataset.page;
    document.getElementById('pageInput').value = page;
    document.getElementById('pageForm').submit();
  });
});