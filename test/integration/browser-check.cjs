const {chromium} = require('playwright');
const path = require('path');
const fs = require('fs');
const assert = require('assert/strict');
(async()=>{
 const browser = await chromium.launch({headless:true,channel:'chrome'});
 const context=await browser.newContext({viewport:{width:1440,height:1000}});
 const page=await context.newPage(); const errors=[]; let checks=0;
 page.on('pageerror',e=>errors.push(e.message));
 function check(v,m){assert(v,m);checks++;}
 const root='http://127.0.0.1:18080';
 for(const app of ['/Group04','/renamed-shop']) {
  let r=await page.goto(root+app+'/',{timeout:120000});check(r.status()===200,'home loads');
  await page.reload({timeout:120000}); // Establish cookie session before checking exact URLs.
  await page.getByRole('link',{name:'Đăng nhập',exact:true}).waitFor();
  check(await page.locator('.product-card').count()>0,'real catalog is rendered');
  check(await page.locator('.header-actions a[href="'+app+'/cart"]').count()===1,'cart link');
  await page.locator('[data-banner-step="1"]').click();
  check(await page.locator('.ht-banner-slide.active').innerText().then(t=>t.includes('Chăm chút')),'banner changes');
  await page.goto(root+app+'/products',{timeout:120000});
  await page.locator('#minPrice').fill('100000');await page.locator('#maxPrice').fill('999999999');
  await page.getByRole('button',{name:'Áp dụng',exact:true}).click();
  await page.waitForURL(/minPrice=100000.*maxPrice=999999999/,{timeout:120000});
  check(await page.locator('#minPrice').inputValue()==='100.000đ','VND display formatting');
  await page.locator('#sort').selectOption('priceAsc');
  await page.waitForURL(/sort=priceAsc/,{timeout:120000});
  check(page.url().includes('minPrice=100000'),'sort retains filter');
  await page.locator('.card-detail-link').first().click();
  await page.waitForURL(/\/product\?id=/,{timeout:120000});
  check(await page.locator('#variant-select').getAttribute('name')==='variantId','variant form contract');
  check(await page.locator('#variant-form input[name="action"]').inputValue()==='add','cart action contract');
  check(await page.locator('#variant-form').getAttribute('action')===app+'/cart','context-safe cart action');
  const available=await page.locator('#variant-select option').evaluateAll(os=>os.filter(o=>Number(o.dataset.stock)>0).map(o=>o.value));
  check(available.length>0,'in-stock variant available');
  await page.locator('#variant-select').selectOption(available[0]);
  check(await page.locator('#add-to-cart').isEnabled(),'buy button enabled');
  await page.locator('#add-to-cart').click();await page.waitForURL('**/login',{timeout:120000});
  check(await page.locator('form').getAttribute('action')===app+'/login','guest add redirects to login');
  for(const route of ['/cart','/checkout','/orders','/profile','/change-password']) {
   const resp=await context.request.get(root+app+route,{maxRedirects:0});check(resp.status()===302,'protected '+route);
  }
  for(const route of ['/admin/dashboard','/admin/categories','/admin/products','/admin/users']) {
   const resp=await context.request.get(root+app+route);check(resp.status()===403,'admin protected '+route);
  }
  r=await page.goto(root+app+'/register');check(r.status()===200,'registration view');
  const invalid=await context.request.post(root+app+'/register',{form:{username:'x',email:'invalid',fullName:'',phone:'',password:'',confirmPassword:''}});
  check(invalid.status()===200 && (await invalid.text()).includes('4-30'),'invalid registration stays on form');
  const malformed=await context.request.get(root+app+'/product?id=invalid');check(malformed.status()===404,'invalid product id');
  const missing=await context.request.get(root+app+'/no-such-page');check(missing.status()===404,'404 handler');
 }
 await page.setViewportSize({width:390,height:844});
 await page.goto(root+'/Group04/',{timeout:120000});
 await page.locator('[data-menu-toggle]').click();check(await page.locator('#public-navigation').isVisible(),'mobile menu');
 check(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1),'mobile home overflow');
 const dir=path.resolve(__dirname,'../../build/ui-check');fs.mkdirSync(dir,{recursive:true});
 await page.screenshot({path:path.join(dir,'home-mobile.png'),fullPage:true});
 await page.goto(root+'/Group04/products',{timeout:120000});
 check(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1),'mobile products overflow');
 check(errors.length===0,'no browser script errors: '+errors.join(';'));
 await browser.close();console.log('PASS: '+checks+' browser/HTTP checks in two context paths.');
})().catch(e=>{console.error(e);process.exit(1)});
