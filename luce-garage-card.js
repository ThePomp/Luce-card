const CSS=`:host{display:block;color-scheme:dark;background:#000;min-height:100%}
*{box-sizing:border-box;-webkit-tap-highlight-color:transparent}
.wrap{display:grid;grid-template-columns:1fr 1fr;gap:6px;padding:6px;max-width:1040px;margin:0 auto;font-family:Inter,"Helvetica Neue",Roboto,Arial,sans-serif;color:#f4f4f2}
.car{container-type:inline-size;min-width:0;display:flex;flex-direction:column;gap:5px;padding:6px;border-radius:20px;background:linear-gradient(180deg,#151618,#08080a);border:1px solid #2a2b2f;box-shadow:inset 0 1px 0 rgba(255,255,255,.1)}
.hero{position:relative;aspect-ratio:1.9/1;border-radius:14px;overflow:hidden;background:radial-gradient(62% 62% at 50% 74%,#4a4c52 0,#1b1c20 62%,#0a0a0c 100%)}
.hero img{position:absolute;left:15%;width:70%;bottom:4%;filter:drop-shadow(0 0 .7px rgba(255,255,255,.75)) drop-shadow(0 8px 8px #000)}
.ph{position:absolute;left:50%;top:58%;transform:translate(-50%,-50%);--mdc-icon-size:30cqw;color:#8d8f95}
.nm{position:absolute;left:9px;top:7px;font-size:clamp(11px,5cqw,17px);font-weight:300;letter-spacing:.2em;text-transform:uppercase}
.age{position:absolute;right:9px;top:9px;font-size:clamp(7px,2.7cqw,10px);font-weight:600;letter-spacing:.12em;color:#d7d7d4}
.bin{display:grid;grid-template-columns:1fr 1.6fr 1fr;align-items:center;gap:2px;padding:5px 6px;border-radius:16px;background:linear-gradient(180deg,#0c0c0e,#000);border:1px solid #26272b;box-shadow:inset 0 1px 0 rgba(255,255,255,.09),inset 0 -12px 18px rgba(255,255,255,.025)}
.dial{position:relative;aspect-ratio:1;min-width:0}
.dial svg{position:absolute;inset:0;width:100%;height:100%}
.t{stroke:#47484d;stroke-width:1.1;stroke-linecap:butt}.t.maj{stroke-width:1.9}.t.l{stroke:#fbfbf8}
.dial.low .t.l{stroke:#ff4b3e}
.n{stroke:#ffc400;stroke-width:2.3;stroke-linecap:round}
.v{position:absolute;inset:0;display:flex;align-items:baseline;justify-content:center;padding-top:32%;gap:1px}
.m .v{padding-top:29%}
.v b{font-weight:200;font-size:6.7cqw;line-height:1;font-variant-numeric:tabular-nums}
.m .v b{font-size:15.5cqw}
.v i{font-style:normal;font-size:clamp(7px,2.6cqw,10px);font-weight:600;color:#d7d7d4}
.dial u{position:absolute;left:0;right:0;bottom:3%;text-align:center;text-decoration:none;font-size:clamp(6.5px,2.4cqw,9px);font-weight:700;letter-spacing:.13em;color:#e8e8e5}
.chg{position:absolute;left:50%;top:15%;transform:translateX(-50%);--mdc-icon-size:clamp(10px,5cqw,16px);color:#ffc400;display:none}
.chg.on{display:block}
.st{display:flex;align-items:center;gap:5px;padding:1px 5px;font-size:clamp(7.5px,2.8cqw,10.5px);font-weight:700;letter-spacing:.11em;color:#f0f0ed;min-height:17px;text-transform:uppercase}
.st ha-icon{--mdc-icon-size:clamp(11px,4.3cqw,15px)}.st.bad{color:#ff6a5e}
.st span{white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.keys{display:grid;gap:4px}
button{font:inherit;color:inherit;border:0;padding:0;margin:0;background:none;cursor:pointer}
.key{position:relative;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:2px;height:clamp(40px,17cqw,58px);min-width:0;border-radius:10px;background:linear-gradient(180deg,#34353a,#191a1d 55%,#121316);border:1px solid #45464c;box-shadow:inset 0 1px 0 rgba(255,255,255,.2),0 2px 3px rgba(0,0,0,.65);transition:transform .08s}
.key:active{transform:translateY(1px);box-shadow:inset 0 1px 3px #000}
.key ha-icon{--mdc-icon-size:clamp(15px,6.3cqw,22px);color:#f6f6f3}
.key span{font-size:clamp(6.8px,2.55cqw,9.5px);font-weight:700;letter-spacing:.05em;color:#fff;text-transform:uppercase;white-space:nowrap}
.key.on::after{content:"";position:absolute;top:3px;left:32%;right:32%;height:2px;border-radius:2px;background:#ffc400;box-shadow:0 0 5px #ffc400}
.key.arm{background:linear-gradient(180deg,#ffd23c,#e2a800);border-color:#ffd95f}.key.arm span,.key.arm ha-icon{color:#000}
.lb{font-size:clamp(6.5px,2.35cqw,9px);font-weight:700;letter-spacing:.17em;color:#cfcfcb;padding:2px 5px 0}
.pr{display:grid;grid-template-columns:1fr 1fr;gap:4px}
.pb{display:flex;align-items:center;justify-content:space-between;gap:4px;height:clamp(32px,13cqw,42px);padding:0 8px;border-radius:10px;background:#060608;border:1px solid #2f3035;font-size:clamp(7px,2.6cqw,10px);font-weight:700;letter-spacing:.06em;color:#fff;min-width:0}
.sw{flex:none;position:relative;width:clamp(24px,10cqw,34px);height:clamp(13px,5.2cqw,18px);border-radius:9px;background:#34353a;transition:.15s}
.sw::after{content:"";position:absolute;top:2px;bottom:2px;left:2px;aspect-ratio:1;border-radius:50%;background:#f4f4f2;transition:.15s}
.on .sw{background:#ffc400}.on .sw::after{left:auto;right:2px;background:#000}
input[type=time]{min-width:0;width:100%;border:0;background:none;color:#fff;font:200 clamp(13px,5.4cqw,20px) Inter,Roboto,sans-serif;font-variant-numeric:tabular-nums;text-align:right;outline:0;padding:0}
input::-webkit-calendar-picker-indicator,input::-webkit-datetime-edit-ampm-field{display:none}
.days{display:grid;grid-template-columns:repeat(7,1fr);gap:3px}
.d{height:clamp(24px,10cqw,32px);border-radius:8px;background:linear-gradient(180deg,#2a2b2f,#141518);border:1px solid #3a3b41;font-size:clamp(9px,3.5cqw,13px);font-weight:600;color:#c8c8c4}
.d.on{background:#ffc400;border-color:#ffd95f;color:#000;box-shadow:0 0 6px rgba(255,196,0,.45)}`;
const esc=s=>String(s==null?'':s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const pt=(r,a)=>{const t=(a-90)*Math.PI/180;return (50+r*Math.cos(t)).toFixed(2)+'" y@="'+(50+r*Math.sin(t)).toFixed(2)};
function dial(k,lab,n,cls){let s='';for(let i=0;i<n;i++){const a=-135+270*i/(n-1),maj=i%((n-1)/4)===0;s+='<line class="t'+(maj?' maj':'')+'" x1="'+pt(maj?38:41,a).replace('y@','y1')+'" x2="'+pt(47,a).replace('y@','y2')+'"/>'}
return '<div class="dial '+cls+'" data-d="'+k+'"><svg viewBox="0 0 100 100">'+s+'<line class="n" x1="50" y1="4" x2="50" y2="17"/></svg><div class="v"><b>-</b><i></i></div><u>'+esc(lab)+'</u>'+(cls==='m'?'<ha-icon class="chg" icon="mdi:lightning-bolt"></ha-icon>':'')+'</div>'}
const I18N={
it:{heat:'Scalda',lock:'Blocca',unlock:'Sblocca',refresh:'Aggiorna',open:'Apri',vent:'Socchiudi',close:'Chiudi',confirm:'Conferma',windows:'Finestrini',schedule:'Programma settimanale',active:'Attiva',time:'Ore',battery:'Batteria',locked:'Bloccata',unlockedS:'Sbloccata',allClosed:'tutto chiuso',isOpen:'Aperto: ',data:'Dati',min:'MIN',hour:'H',day:'G',days:['L','M','M','G','V','S','D'],doors:['Ant Sx','Ant Dx','Post Sx','Post Dx','Baule','Cofano'],wins:['Fin Ant Sx','Fin Ant Dx','Fin Post Sx','Fin Post Dx']},
en:{heat:'Heat',lock:'Lock',unlock:'Unlock',refresh:'Refresh',open:'Open',vent:'Vent',close:'Close',confirm:'Confirm',windows:'Windows',schedule:'Weekly schedule',active:'Active',time:'Time',battery:'Battery',locked:'Locked',unlockedS:'Unlocked',allClosed:'all closed',isOpen:'Open: ',data:'Data',min:'MIN',hour:'H',day:'D',days:['M','T','W','T','F','S','S'],doors:['Front L','Front R','Rear L','Rear R','Trunk','Hood'],wins:['Win FL','Win FR','Win RL','Win RR']}};
const PRESET={battery:'sensor.{p}_livello_batteria_ev',range:'sensor.{p}_autonomia_ev',aux_battery:'sensor.{p}_livello_batteria_auto',updated:'sensor.{p}_ultimo_aggiornamento',charging:'binary_sensor.{p}_ricarica_batteria_ev',climate:'binary_sensor.{p}_aria_condizionata',lock:'lock.{p}_serratura_porte',refresh:'button.{p}_forza_aggiornamento',windows_open:'button.{p}_apri_tutti_i_finestrini',windows_vent:'button.{p}_socchiudi_tutti_i_finestrini',windows_close:'button.{p}_chiudi_tutti_i_finestrini'};
const PD=['porta_anteriore_sinistra','porta_anteriore_destra','porta_posteriore_sinistra','porta_posteriore_destra','bagagliaio','cofano'];
const PW=['finestrino_anteriore_sinistro','finestrino_anteriore_destro','finestrino_posteriore_sinistro','finestrino_posteriore_destro'];
const DK=['mon','tue','wed','thu','fri','sat','sun'];
class LuceGarageCard extends HTMLElement{
static getStubConfig(){return{cars:[{name:'My EV',prefix:'my_ev',schedule_id:'my_ev'}]}}
setConfig(c){if(!c||!Array.isArray(c.cars)||!c.cars.length)throw new Error('luce-garage-card: define at least one car under "cars"');c.cars.forEach((x,i)=>{if(!x||!x.name)throw new Error('luce-garage-card: car #'+(i+1)+' needs a "name"')});this.c=c;this.built=false}
getCardSize(){return 10}
getGridOptions(){return{columns:'full',rows:'auto'}}
set hass(h){this.h=h;if(!this.built)this.build();this.upd()}
tr(){const l=(this.c.language||(this.h.locale&&this.h.locale.language)||this.h.language||'en').slice(0,2);return I18N[l]||I18N.en}
ent(c,k){const e=c.entities||{};if(e[k])return e[k];return c.prefix&&PRESET[k]?PRESET[k].replace('{p}',c.prefix):''}
has(e){return !!(e&&this.h.states[e])}
list(c,k,pat,labels){const e=c.entities||{};if(Array.isArray(e[k]))return e[k].map((x,i)=>typeof x==='string'?{entity:x,name:labels[i]||x}:x);return c.prefix?pat.map((s,i)=>({entity:'binary_sensor.'+c.prefix+'_'+s,name:labels[i]})):[]}
sched(c){const s=c.schedule||{};if(s.enabled||s.time||s.days)return{enabled:s.enabled||'',time:s.time||'',days:s.days||[]};if(!c.schedule_id)return null;const p=c.schedule_id;return{enabled:'input_boolean.'+p+'_schedule_enabled',time:'input_datetime.'+p+'_schedule_time',days:DK.map(d=>'input_boolean.'+p+'_schedule_'+d)}}
add(i,l,dom,sv,ent,conf,lit){if(!this.has(ent))return null;const k={i,l,dom,sv,ent,conf,lit,id:this.ks.length};this.ks.push(k);return k}
kh(a){return a.filter(Boolean).map(k=>'<button class="key" data-k="'+k.id+'"><ha-icon icon="'+esc(k.i)+'"></ha-icon><span>'+esc(k.l)+'</span></button>').join('')}
build(){this.built=true;this.ks=[];if(!this.shadowRoot)this.attachShadow({mode:'open'});const T=this.tr();
this.cars=this.c.cars.map(c=>{const m={};Object.keys(PRESET).concat(['heat']).forEach(k=>m[k]=this.ent(c,k));return{c,m,doors:this.list(c,'doors',PD,T.doors).filter(d=>this.has(d.entity)),wins:this.list(c,'windows',PW,T.wins).filter(d=>this.has(d.entity)),s:this.sched(c)}});
const html=this.cars.map((o,n)=>{const c=o.c,m=o.m,lk=m.lock,ks=[this.add(c.heat_icon||'mdi:car-seat-heater',T.heat,'script','turn_on',m.heat,1,m.climate),this.add('mdi:lock',T.lock,'lock','lock',lk,0,lk+'=locked'),this.add('mdi:lock-open-variant',T.unlock,'lock','unlock',lk,1,lk+'=unlocked'),this.add('mdi:refresh',T.refresh,'button','press',m.refresh,1,'')],ws=[this.add('mdi:window-open-variant',T.open,'button','press',m.windows_open,1,''),this.add('mdi:window-maximize',T.vent,'button','press',m.windows_vent,1,''),this.add('mdi:window-closed-variant',T.close,'button','press',m.windows_close,0,'')],kc=ks.filter(Boolean).length,wc=ws.filter(Boolean).length,s=o.s,sOn=s&&(this.has(s.enabled)||this.has(s.time)||s.days.some(d=>this.has(d)));
const hero=c.image?'<img alt="" src="'+esc(c.image)+'">':'<ha-icon class="ph" icon="mdi:car-electric"></ha-icon>';
return '<section class="car" data-c="'+n+'"><div class="hero">'+hero+'<div class="nm">'+esc(c.name)+'</div><div class="age"></div></div><div class="bin">'+dial('r','KM',21,'s')+dial('b',T.battery.toUpperCase(),41,'m')+dial('v','12 V',21,'s')+'</div><div class="st"><ha-icon></ha-icon><span></span></div>'+(kc?'<div class="keys" style="grid-template-columns:repeat('+kc+',1fr)">'+this.kh(ks)+'</div>':'')+(wc?'<div class="lb">'+esc(T.windows.toUpperCase())+'</div><div class="keys" style="grid-template-columns:repeat('+wc+',1fr)">'+this.kh(ws)+'</div>':'')+(sOn?'<div class="lb">'+esc(T.schedule.toUpperCase())+'</div><div class="pr">'+(this.has(s.enabled)?'<button class="pb" data-pg><span>'+esc(T.active.toUpperCase())+'</span><i class="sw"></i></button>':'')+(this.has(s.time)?'<label class="pb"><span>'+esc(T.time.toUpperCase())+'</span><input type="time"></label>':'')+'</div><div class="days">'+s.days.slice(0,7).map((d,i)=>this.has(d)?'<button class="d" data-day="'+i+'">'+esc(T.days[i])+'</button>':'').join('')+'</div>':'')+'</section>'}).join('');
const cols=Math.min(this.c.columns||2,this.cars.length)||1;
this.shadowRoot.innerHTML='<style>'+CSS+'</style><div class="wrap" style="grid-template-columns:repeat('+cols+',minmax(0,1fr))">'+html+'</div>';
const R=this.shadowRoot;R.querySelectorAll('.key').forEach(b=>b.onclick=()=>this.press(b));
R.querySelectorAll('.car').forEach(el=>{const o=this.cars[+el.dataset.c],s=o.s,pg=el.querySelector('[data-pg]'),t=el.querySelector('input');if(pg)pg.onclick=()=>this.call('input_boolean','toggle',s.enabled);el.querySelectorAll('[data-day]').forEach(b=>b.onclick=()=>this.call('input_boolean','toggle',s.days[+b.dataset.day]));if(t)t.onchange=()=>{if(t.value)this.h.callService('input_datetime','set_datetime',{time:t.value+':00'},{entity_id:s.time})}})}
call(d,s,e){this.h.callService(d,s,{},{entity_id:e})}
press(b){const k=this.ks[+b.dataset.k],sp=b.querySelector('span');
if(k.conf&&!b.classList.contains('arm')){b.classList.add('arm');sp.textContent=this.tr().confirm;b._t=setTimeout(()=>{b.classList.remove('arm');sp.textContent=k.l},3000);return}
clearTimeout(b._t);b.classList.remove('arm');sp.textContent=k.l;this.call(k.dom,k.sv,k.ent)}
s(e){const o=e&&this.h.states[e];return o?o.state:''}
num(e){const v=parseFloat(this.s(e));return isNaN(v)?null:v}
setD(root,k,val,max,unit,low){const d=root.querySelector('[data-d="'+k+'"]'),ok=val!==null&&!isNaN(val),f=ok?Math.max(0,Math.min(1,val/max)):0,ts=d.querySelectorAll('.t'),m=ok?Math.round(f*(ts.length-1)):-1;
ts.forEach((t,i)=>t.classList.toggle('l',i<=m));d.querySelector('.n').setAttribute('transform','rotate('+(-135+270*f)+' 50 50)');d.querySelector('b').textContent=ok?Math.round(val):'-';d.querySelector('i').textContent=unit;d.classList.toggle('low',ok&&val<low)}
upd(){const R=this.shadowRoot,T=this.tr();
R.querySelectorAll('.car').forEach(el=>{const o=this.cars[+el.dataset.c],c=o.c,m=o.m,s=o.s;
this.setD(el,'r',this.num(m.range),c.max_range||600,'',40);this.setD(el,'b',this.num(m.battery),100,'%',20);this.setD(el,'v',this.num(m.aux_battery),100,'%',60);
el.querySelector('.chg').classList.toggle('on',this.s(m.charging)==='on');
const us=this.s(m.updated),mi=us?(Date.now()-new Date(us))/6e4:NaN;el.querySelector('.age').textContent=isNaN(mi)?'':T.data.toUpperCase()+' '+(mi<60?Math.max(0,Math.round(mi))+' '+T.min:mi<1440?Math.round(mi/60)+' '+T.hour:Math.round(mi/1440)+' '+T.day);
const list=o.doors.concat(o.wins).filter(d=>this.s(d.entity)==='on').map(d=>d.name),lk=this.s(m.lock),st=el.querySelector('.st'),bad=list.length>0;
st.classList.toggle('bad',bad||lk==='unlocked');st.querySelector('ha-icon').setAttribute('icon',lk==='unlocked'?'mdi:lock-open-variant':lk==='locked'?'mdi:lock':'mdi:car-door');st.querySelector('span').textContent=bad?T.isOpen+list.join(' - '):(lk==='locked'?T.locked+' - ':lk==='unlocked'?T.unlockedS+' - ':'')+T.allClosed;
if(s){el.querySelectorAll('[data-day]').forEach(b=>b.classList.toggle('on',this.s(s.days[+b.dataset.day])==='on'));const pg=el.querySelector('[data-pg]');if(pg)pg.classList.toggle('on',this.s(s.enabled)==='on');const t=el.querySelector('input'),v=this.s(s.time).slice(0,5);if(t&&v&&R.activeElement!==t&&t.value!==v)t.value=v}});
R.querySelectorAll('.key').forEach(b=>{const l=this.ks[+b.dataset.k].lit;if(!l)return;const q=l.split('=');b.classList.toggle('on',this.s(q[0])===(q[1]||'on'))})}
}
if(!customElements.get('luce-garage-card'))customElements.define('luce-garage-card',LuceGarageCard);
window.customCards=window.customCards||[];window.customCards.push({type:'luce-garage-card',name:'Luce Garage Card',description:'Compact instrument-style control card for electric cars'});
console.info('luce-garage-card 1.0.0');
