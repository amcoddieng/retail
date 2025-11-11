(function(){
    var input = document.getElementById('cmdk-input');
    var list = document.getElementById('cmdk-list');
    var dialog = document.getElementById('cmdk');
    if(!input || !list || !dialog) return;

    function search(q){
        list.innerHTML = '';
        (window.CMDK_ITEMS||[])
            .filter(i => i.label.toLowerCase().includes(q.toLowerCase()))
            .slice(0,10)
            .forEach(i=>{
                var li=document.createElement('li');
                li.textContent=i.label;
                li.tabIndex=0;
                li.style.cursor='pointer';
                li.onclick=function(){ window.location = i.path };
                list.appendChild(li);
            });
    }

    input.addEventListener('input', function(){ search(this.value) });

    document.addEventListener('keydown', function(e){
        if((e.ctrlKey||e.metaKey) && e.key.toLowerCase()==='k'){
            e.preventDefault();
            dialog.style.display='block';
            input.focus();
            search('');
        } else if(e.key==='Escape'){
            dialog.style.display='none';
        }
    });
})();
function chartExtender() {
    // Personnalisation du graphique
    if (this.options) {
        this.options.responsive = true;
        this.options.maintainAspectRatio = false;
        this.options.plugins = {
            legend: {
                display: true,
                position: 'top'
            },
            tooltip: {
                mode: 'index',
                intersect: false
            }
        };
        this.options.scales = {
            y: {
                beginAtZero: true,
                ticks: {
                    precision: 0
                }
            }
        };
    }
}

function toggleMobileNav(){
  var el = document.getElementById('mobileNav');
  if(!el) return;
  el.style.display = (el.style.display === 'block') ? 'none' : 'block';
}

// Démo : remplace par ta page / logique de recherche si tu en as une
function goSearch(q){
  if(!q) return;
  window.location = (window.contextPath || '') + '/index.xhtml?query=' + encodeURIComponent(q);
}

// Thème clair/sombre basique (tu peux brancher sur PrimeFaces ThemeSwitcher si besoin)
function toggleTheme(){
  document.documentElement.classList.toggle('dark');
}
