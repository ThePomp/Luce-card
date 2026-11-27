// Luce Wallet Card - instrument-style net worth card for Home Assistant.
const CSS=`:host{display:block;color-scheme:dark;background:#000;min-height:100%}
*{box-sizing:border-box;-webkit-tap-highlight-color:transparent}
.wrap{max-width:560px;margin:0 auto;padding:6px;font-family:Inter,"Helvetica Neue",Roboto,Arial,sans-serif;color:#f4f4f2}
.w{container-type:inline-size;display:flex;flex-direction:column;gap:6px;padding:6px;border-radius:20px;background:linear-gradient(180deg,#151618,#08080a);border:1px solid #2a2b2f;box-shadow:inset 0 1px 0 rgba(255,255,255,.1)}
.hero{position:relative;padding:10px 12px 6px;border-radius:14px;background:radial-gradient(80% 90% at 50% 100%,#2a2b30 0,#141518 60%,#0a0a0c 100%);border:1px solid #232428}
.top{display:flex;justify-content:space-between;align-items:center;gap:8px}
.nm{font-size:clamp(11px,3.6cqw,16px);font-weight:300;letter-spacing:.2em;text-transform:uppercase;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.age{font-size:clamp(8px,2cqw,10px);font-weight:600;letter-spacing:.12em;color:#9a9b9f;white-space:nowrap}
.big{display:flex;align-items:baseline;gap:2px;margin-top:6px;font-variant-numeric:tabular-nums;cursor:pointer;user-select:none}
.big b{font-weight:200;font-size:clamp(34px,12.5cqw,64px);line-height:1;letter-spacing:-.01em}
.big i{font-style:normal;font-weight:300;font-size:clamp(14px,4.6cqw,24px);color:#cfcfcb}
.big ha-icon{--mdc-icon-size:clamp(13px,3.6cqw,18px);color:#6d6e73;margin-left:6px;align-self:center}
.dl{display:flex;align-items:center;gap:6px;margin-top:4px;min-height:20px;font-size:clamp(11px,3.4cqw,15px);font-weight:600;font-variant-numeric:tabular-nums;color:#cfcfcb}
.dl ha-icon{--mdc-icon-size:clamp(14px,4cqw,18px)}
.dl.up{color:#25c43c}.dl.dn{color:#ff5a4f}
.dl span{font-size:clamp(8px,2.2cqw,10px);font-weight:700;letter-spacing:.14em;color:#9a9b9f;margin-left:auto}
.ch{position:relative;height:clamp(70px,24cqw,120px);margin-top:4px}
.ch svg{position:absolute;inset:0;width:100%;height:100%;overflow:visible;touch-action:pan-y}
.ch .ln{fill:none;stroke:#ffc400;stroke-width:2;stroke-linejoin:round;stroke-linecap:round;vector-effect:non-scaling-stroke}
.ch .ar{fill:url(#lwg);stroke:none}
.ch .bs{stroke:#3a3b40;stroke-width:1;stroke-dasharray:2 3;vector-effect:non-scaling-stroke}
.ch .xh{stroke:#8d8f95;stroke-width:1;vector-effect:non-scaling-stroke;display:none}
.ch .dot{position:absolute;width:10px;height:10px;margin:-5px 0 0 -5px;border-radius:50%;background:#ffc400;border:2px solid #0c0c0e;display:none;pointer-events:none}
.tip{position:absolute;top:0;transform:translateX(-50%);padding:3px 7px;border-radius:7px;background:#232428;border:1px solid #3a3b41;font-size:11px;font-weight:600;white-space:nowrap;font-variant-numeric:tabular-nums;display:none;pointer-events:none;z-index:2}
.tip small{display:block;font-size:9px;font-weight:600;color:#9a9b9f;letter-spacing:.06em}
.ch .em{position:absolute;inset:0;pointer-events:none;display:flex;align-items:center;justify-content:center;font-size:10px;letter-spacing:.14em;color:#6d6e73;text-transform:uppercase}
.keys{display:grid;grid-template-columns:repeat(5,1fr);gap:4px}
button{font:inherit;color:inherit;border:0;padding:0;margin:0;background:none;cursor:pointer}
.key{position:relative;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:1px;height:clamp(40px,11cqw,52px);min-width:0;border-radius:10px;background:linear-gradient(180deg,#34353a,#191a1d 55%,#121316);border:1px solid #45464c;box-shadow:inset 0 1px 0 rgba(255,255,255,.2),0 2px 3px rgba(0,0,0,.65);transition:transform .08s}
.key:active{transform:translateY(1px);box-shadow:inset 0 1px 3px #000}
.key b{font-size:clamp(11px,3.3cqw,15px);font-weight:700;letter-spacing:.06em;color:#fff}
.key small{font-size:clamp(7.5px,2.2cqw,10px);font-weight:700;font-variant-numeric:tabular-nums;color:#9a9b9f;white-space:nowrap}
.key small.up{color:#25c43c}.key small.dn{color:#ff5a4f}
.key.on::after{content:"";position:absolute;top:3px;left:32%;right:32%;height:2px;border-radius:2px;background:#ffc400;box-shadow:0 0 5px #ffc400}
.lb{display:flex;justify-content:space-between;font-size:clamp(7.5px,2.2cqw,10px);font-weight:700;letter-spacing:.17em;color:#cfcfcb;padding:4px 6px 0;text-transform:uppercase}
.lb span{color:#8d8f95;font-variant-numeric:tabular-nums;letter-spacing:.08em}
.seg{display:flex;gap:2px;height:8px;margin:0 4px}
.seg i{height:100%;border-radius:4px;min-width:2px}
.rows{display:flex;flex-direction:column;gap:3px}
.row{display:grid;grid-template-columns:auto 1fr auto auto;align-items:center;gap:8px;min-height:clamp(38px,10cqw,46px);padding:4px 10px;border-radius:10px;background:#060608;border:1px solid #26272b;text-align:left;width:100%}
.row:active{background:#111214}
.sw{width:10px;height:10px;border-radius:3px}
.rn{min-width:0;display:flex;flex-direction:column;gap:1px}
.rn b{font-size:clamp(11px,3.3cqw,14px);font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.rn small{font-size:clamp(8px,2.3cqw,10.5px);font-weight:600;font-variant-numeric:tabular-nums;color:#8d8f95;white-space:nowrap}
.rn small.up{color:#25c43c}.rn small.dn{color:#ff5a4f}
.rs{font-size:clamp(9px,2.6cqw,11px);font-weight:600;color:#8d8f95;font-variant-numeric:tabular-nums;text-align:right}
.rv{font-size:clamp(12px,3.6cqw,16px);font-weight:300;font-variant-numeric:tabular-nums;text-align:right;white-space:nowrap}
.row.x{opacity:.62}
.row.x .rs{font-size:clamp(7px,2cqw,9px);letter-spacing:.1em;text-transform:uppercase}
.mask .rv,.mask .big b,.mask .big i{letter-spacing:.12em}
.mask .big b{font-size:clamp(22px,7cqw,36px);line-height:1.6}`;
const esc=s=>String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
// Categorical slots stepped for a dark surface (validated: CVD, contrast and lightness band). Color follows config order, never rank.
const PAL=['#338ced','#e76624','#00a888','#d28e05','#e24d8b','#0aac23','#9887ef','#eb6263'],OTHER='#6d6e73';
const PER=[{k:'day',ms:864e5,p:'5minute'},{k:'week',ms:7*864e5,p:'hour'},{k:'month',ms:30*864e5,p:'day'},{k:'half',ms:182*864e5,p:'day'},{k:'year',ms:365*864e5,p:'day'}];
const I18N={
it:{title:'Patrimonio',data:'Dati',min:'MIN',hour:'H',dayU:'G',split:'Ripartizione',accounts:'conti',out:'fuori totale',keys:{day:'1G',week:'1S',month:'1M',half:'6M',year:'1A'},long:{day:'oggi',week:'1 settimana',month:'1 mese',half:'6 mesi',year:'1 anno'},nodata:'Nessuno storico',loading:'Carico...'},
en:{title:'Net worth',data:'Data',min:'MIN',hour:'H',dayU:'D',split:'Allocation',accounts:'accounts',out:'not in total',keys:{day:'1D',week:'1W',month:'1M',half:'6M',year:'1Y'},long:{day:'today',week:'1 week',month:'1 month',half:'6 months',year:'1 year'},nodata:'No history',loading:'Loading...'}};
class LuceWalletCard extends HTMLElement{
static getStubConfig(){return{total:'sensor.net_worth',accounts:[{name:'Bank',entity:'sensor.bank_balance'}]}}
setConfig(c){if(!c||(!c.total&&!(Array.isArray(c.accounts)&&c.accounts.length)))throw new Error('luce-wallet-card: set "total" or at least one entry under "accounts"');(c.accounts||[]).forEach((a,i)=>{if(!a||!a.entity)throw new Error('luce-wallet-card: account #'+(i+1)+' needs an "entity"')});this.c=c;this.built=false;this.cache={};this.sel=PER.some(p=>p.k===c.default_period)?c.default_period:'day';this.mask=localStorage.getItem('luce-wallet-mask')==='1'}
getCardSize(){return 8}
getGridOptions(){return{columns:'full',rows:'auto'}}
set hass(h){this.h=h;if(!this.built)this.build();this.upd();this.load()}
lang(){return(this.c.language||(this.h.locale&&this.h.locale.language)||this.h.language||'en').slice(0,2)}
tr(){return I18N[this.lang()]||I18N.en}
s(e){const o=e&&this.h.states[e];return o?o.state:''}
num(e){const v=parseFloat(this.s(e));return isNaN(v)?null:v}
unit(e){const o=e&&this.h.states[e];return o&&o.attributes.unit_of_measurement||''}
ids(a){return Array.isArray(a.entity)?a.entity:[a.entity]}
aval(a){let t=null;this.ids(a).forEach(e=>{const v=this.num(e);if(v!==null)t=(t||0)+v});return t}
cur(){return(this.c.currency||this.unit(this.c.total)||this.unit(this.ids((this.c.accounts||[])[0]||{entity:''})[0])||'EUR').toUpperCase()}
fmt(v,d,sign){if(v===null||isNaN(v))return'-';const cc=this.cur(),o={minimumFractionDigits:d,maximumFractionDigits:d,signDisplay:sign?'exceptZero':'auto'};let r;try{r=new Intl.NumberFormat(this.lang(),Object.assign({style:'currency',currency:cc},o)).format(v)}catch(e){r=new Intl.NumberFormat(this.lang(),o).format(v)+' '+cc}return r}
pct(v){return v===null||isNaN(v)?'-':new Intl.NumberFormat(this.lang(),{minimumFractionDigits:2,maximumFractionDigits:2,signDisplay:'exceptZero'}).format(v)+'%'}
cls(v){return v>0?'up':v<0?'dn':''}
total(){const t=this.num(this.c.total);if(t!==null)return t;let s=null;(this.c.accounts||[]).forEach(a=>{if(a.in_total===false)return;const v=this.aval(a);if(v!==null)s=(s||0)+v});return s}
build(){this.built=true;if(!this.shadowRoot)this.attachShadow({mode:'open'});const T=this.tr(),c=this.c;
this.acc=(c.accounts||[]).map((a,i)=>({a,i,col:a.color||(a.in_total===false?OTHER:PAL[i]||OTHER)}));
const keys=PER.map(p=>'<button class="key" data-p="'+p.k+'"><b>'+esc(T.keys[p.k])+'</b><small></small></button>').join('');
const rows=this.acc.map(o=>'<button class="row'+(o.a.in_total===false?' x':'')+'" data-a="'+o.i+'"><i class="sw" style="background:'+esc(o.col)+'"></i><span class="rn"><b>'+esc(o.a.name||this.ids(o.a)[0])+'</b><small></small></span><span class="rs"></span><span class="rv"></span></button>').join('');
this.shadowRoot.innerHTML='<style>'+CSS+'</style><div class="wrap"><section class="w"><div class="hero"><div class="top"><div class="nm">'+esc(c.title||T.title)+'</div><div class="age"></div></div><div class="big" role="button" tabindex="0"><b>-</b><i></i><ha-icon></ha-icon></div><div class="dl"><ha-icon></ha-icon><em style="font-style:normal"></em><span></span></div><div class="ch"><svg viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true"><defs><linearGradient id="lwg" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ffc400" stop-opacity=".22"/><stop offset="1" stop-color="#ffc400" stop-opacity="0"/></linearGradient></defs><path class="ar"/><line class="bs" x1="0" x2="100"/><path class="ln"/><line class="xh" y1="0" y2="100"/></svg><div class="dot"></div><div class="tip"></div><div class="em"></div></div></div>'+(c.show_periods===false?'':'<div class="keys">'+keys+'</div>')+(rows?'<div class="lb">'+esc(T.split)+'<span></span></div><div class="seg"></div><div class="rows">'+rows+'</div>':'')+'</section></div>';
const R=this.shadowRoot;
R.querySelectorAll('.key').forEach(b=>b.onclick=()=>{this.sel=b.dataset.p;this.upd();this.load()});
R.querySelectorAll('.row').forEach(b=>b.onclick=()=>this.more(this.ids(this.acc[+b.dataset.a].a)[0]));
const big=R.querySelector('.big'),tg=()=>{this.mask=!this.mask;localStorage.setItem('luce-wallet-mask',this.mask?'1':'0');this.upd()};big.onclick=tg;big.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();tg()}};
const sv=R.querySelector('.ch svg');sv.onpointermove=e=>this.hover(e);sv.onpointerdown=e=>this.hover(e);sv.onpointerleave=()=>this.hover(null)}
more(e){if(!e)return;const ev=new Event('hass-more-info',{bubbles:true,composed:true});ev.detail={entityId:e};this.dispatchEvent(ev)}
statId(){return this.c.history||this.c.total}
load(){PER.forEach(p=>this.load1(p))}
async load1(p){const id=this.statId();if(!id||(this.c.show_periods===false&&p.k!==this.sel))return;const k=p.k,ca=this.cache[k];if(ca&&(ca.busy||Date.now()-ca.t<6e5))return;
this.cache[k]={busy:true,t:Date.now(),pts:ca&&ca.pts};if(!ca)this.upd();
try{const r=await this.h.callWS({type:'recorder/statistics_during_period',start_time:new Date(Date.now()-p.ms).toISOString(),period:p.p,statistic_ids:[id],types:['mean']}),l=(r&&r[id])||[];
this.cache[k]={t:Date.now(),pts:l.filter(x=>x.mean!=null).map(x=>({t:typeof x.start==='number'?x.start:Date.parse(x.start),v:x.mean}))}}catch(e){this.cache[k]={t:Date.now(),pts:[],err:1}}
this.upd()}
series(){const ca=this.cache[this.sel];if(!ca||!ca.pts)return null;const pts=ca.pts.slice(),now=this.num(this.statId());if(now!==null)pts.push({t:Date.now(),v:now});return pts}
delta(k){const ca=this.cache[k],now=this.num(this.statId());if(!ca||!ca.pts||!ca.pts.length||now===null)return null;const b=ca.pts[0].v;return{abs:now-b,pct:b?(now-b)/b*100:null}}
pdelta(k){if(k==='day'&&this.c.change){const p=this.num(this.c.change),u=this.unit(this.c.change);if(p!==null)return u==='%'?{pct:p,abs:this.num(this.c.change_amount)}:{abs:p,pct:null}}return this.delta(k)}
when(t){const d=new Date(t),o=this.sel==='day'?{hour:'2-digit',minute:'2-digit'}:this.sel==='week'?{weekday:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}:this.sel==='month'?{day:'numeric',month:'short'}:{day:'numeric',month:'short',year:'2-digit'};return d.toLocaleString(this.lang(),o)}
draw(){const R=this.shadowRoot,T=this.tr(),pts=this.series(),ln=R.querySelector('.ln'),ar=R.querySelector('.ar'),bs=R.querySelector('.bs'),em=R.querySelector('.em');this.geo=null;
if(!pts||pts.length<2){ln.setAttribute('d','');ar.setAttribute('d','');bs.style.display='none';em.textContent=this.cache[this.sel]&&!this.cache[this.sel].busy?T.nodata:T.loading;return}
em.textContent='';let lo=Infinity,hi=-Infinity;pts.forEach(p=>{lo=Math.min(lo,p.v);hi=Math.max(hi,p.v)});const pad=(hi-lo)*.12||Math.abs(hi)*.002||1;lo-=pad;hi+=pad;const t0=pts[0].t,t1=pts[pts.length-1].t||t0+1,X=t=>(t-t0)/(t1-t0||1)*100,Y=v=>(1-(v-lo)/(hi-lo))*100;
const d=pts.map((p,i)=>(i?'L':'M')+X(p.t).toFixed(2)+' '+Y(p.v).toFixed(2)).join(' ');ln.setAttribute('d',d);ar.setAttribute('d',d+' L100 100 L0 100 Z');const by=Y(pts[0].v).toFixed(2);bs.setAttribute('y1',by);bs.setAttribute('y2',by);bs.style.display='';this.geo={pts,X,Y}}
hover(e){const R=this.shadowRoot,xh=R.querySelector('.xh'),dot=R.querySelector('.dot'),tip=R.querySelector('.tip'),g=this.geo;
if(!e||!g){xh.style.display=dot.style.display=tip.style.display='none';return}
const box=R.querySelector('.ch').getBoundingClientRect(),fx=Math.max(0,Math.min(1,(e.clientX-box.left)/box.width))*100;let b=g.pts[0],bd=Infinity;g.pts.forEach(p=>{const dd=Math.abs(g.X(p.t)-fx);if(dd<bd){bd=dd;b=p}});
const x=g.X(b.t),y=g.Y(b.v);xh.setAttribute('x1',x);xh.setAttribute('x2',x);xh.style.display='';dot.style.left=x+'%';dot.style.top=y+'%';dot.style.display='block';
tip.innerHTML='<small>'+esc(this.when(b.t))+'</small>'+esc(this.mask?'\u2022\u2022\u2022\u2022\u2022':this.fmt(b.v,0));tip.style.left=Math.max(14,Math.min(86,x))+'%';tip.style.top=(y<40?'auto':'0');tip.style.bottom=(y<40?'0':'auto');tip.style.display='block'}
age(){const T=this.tr(),ids=[this.c.total].concat(...(this.c.accounts||[]).map(a=>this.ids(a))).filter(e=>this.h.states[e]);if(!ids.length)return'';const t=Math.max(...ids.map(e=>Date.parse(this.h.states[e].last_updated)||0)),mi=(Date.now()-t)/6e4;if(!t)return'';return T.data.toUpperCase()+' '+(mi<60?Math.max(0,Math.round(mi))+' '+T.min:mi<1440?Math.round(mi/60)+' '+T.hour:Math.round(mi/1440)+' '+T.dayU)}
upd(){if(!this.built)return;const R=this.shadowRoot,T=this.tr(),M=this.mask,hide='\u2022\u2022\u2022\u2022\u2022',tot=this.total();
R.querySelector('.w').classList.toggle('mask',M);R.querySelector('.age').textContent=this.age();
const f=this.fmt(tot,2),m=f.match(/^(.*?)([.,]\d{2})(\D*)$/),big=R.querySelector('.big');big.querySelector('b').textContent=M?hide:m?m[1]:f;big.querySelector('i').textContent=M?'':m?m[2]+m[3]:'';big.querySelector('ha-icon').setAttribute('icon',M?'mdi:eye-off-outline':'mdi:eye-outline');big.setAttribute('aria-label',M?'':f);
const dv=this.pdelta(this.sel),dl=R.querySelector('.dl'),v=dv?(dv.pct!=null?dv.pct:dv.abs):null;dl.className='dl '+this.cls(v);dl.querySelector('ha-icon').setAttribute('icon',v>0?'mdi:triangle':v<0?'mdi:triangle-down':'mdi:minus');dl.querySelector('ha-icon').style.setProperty('--mdc-icon-size',v?'12px':'');
dl.querySelector('em').textContent=dv?[dv.pct!=null?this.pct(dv.pct):'',dv.abs!=null?(M?hide:this.fmt(dv.abs,0,true)):''].filter(Boolean).join('  \u00b7  '):'-';dl.querySelector('span').textContent=T.long[this.sel].toUpperCase();
R.querySelectorAll('.key').forEach(b=>{const k=b.dataset.p,d=this.pdelta(k),x=d?(d.pct!=null?d.pct:d.abs):null,sm=b.querySelector('small');b.classList.toggle('on',k===this.sel);sm.className=this.cls(x);sm.textContent=d?(d.pct!=null?this.pct(d.pct):M?hide:this.fmt(d.abs,0,true)):'\u00a0'});
this.draw();
if(!this.acc.length)return;const vals=this.acc.map(o=>({o,v:this.aval(o.a)})),inT=vals.filter(x=>x.o.a.in_total!==false&&x.v>0),sum=inT.reduce((s,x)=>s+x.v,0),ordered=vals.slice().sort((a,b)=>(a.o.a.in_total===false)-(b.o.a.in_total===false)||(b.v||0)-(a.v||0));
R.querySelector('.lb span').textContent=inT.length+' '+T.accounts;
R.querySelector('.seg').innerHTML=ordered.filter(x=>x.o.a.in_total!==false&&x.v>0).map(x=>'<i title="'+esc((x.o.a.name||'')+' '+(sum?(x.v/sum*100).toFixed(1)+'%':''))+'" style="flex:'+x.v+' 0 0;background:'+esc(x.o.col)+'"></i>').join('');
const box=R.querySelector('.rows');ordered.forEach(x=>{const el=box.querySelector('[data-a="'+x.o.i+'"]'),a=x.o.a;box.appendChild(el);el.querySelector('.rv').textContent=M?hide:this.fmt(x.v,0);
el.querySelector('.rs').textContent=a.in_total===false?T.out:sum&&x.v!==null?(x.v/sum*100).toFixed(1).replace('.',this.lang()==='en'?'.':',')+'%':'';
const sm=el.querySelector('.rn small');let tx='',cv=null;if(a.change){const cn=this.num(a.change),u=this.unit(a.change);if(cn!==null){cv=cn;tx=u==='%'?this.pct(cn):M?hide:this.fmt(cn,0,true);if(a.change_label)tx+=' '+a.change_label}}else if(a.subtitle)tx=a.subtitle;sm.className=this.cls(cv);sm.textContent=tx;sm.style.display=tx?'':'none'})}
}
if(!customElements.get('luce-wallet-card'))customElements.define('luce-wallet-card',LuceWalletCard);
window.customCards=window.customCards||[];window.customCards.push({type:'luce-wallet-card',name:'Luce Wallet Card',description:'Instrument-style net worth card: total, period returns and allocation'});
console.info('luce-wallet-card 1.0.0');
