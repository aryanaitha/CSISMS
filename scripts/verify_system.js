async function verifyAll() {
  try {
    // 1. Static HTML
    const htmlRes = await fetch('http://localhost:8080/');
    const htmlText = await htmlRes.text();
    console.log('1. Static index.html served:', htmlRes.status, 'Has html:', htmlText.includes('<html') || htmlText.includes('<!DOCTYPE html>'));

    // 2. Health
    const healthRes = await fetch('http://localhost:8080/api/health');
    const healthData = await healthRes.json();
    console.log('2. Health check:', healthRes.status, JSON.stringify(healthData));

    // 3. Login as Admin
    const loginRes = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'admin', password: 'Admin@123' })
    });
    const cookie = loginRes.headers.get('set-cookie');
    console.log('3. Admin login:', loginRes.status, 'Session cookie:', cookie ? cookie.split(';')[0] : 'None');

    // 4. Safety Inspections & Violations
    const inspRes = await fetch('http://localhost:8080/api/safety/inspections', { headers: { 'Cookie': cookie } });
    const inspData = await inspRes.json();
    console.log('4. Safety inspections:', inspRes.status, 'Count:', inspData.data ? inspData.data.length : 'none');

    const violRes = await fetch('http://localhost:8080/api/safety/violations', { headers: { 'Cookie': cookie } });
    const violData = await violRes.json();
    console.log('   Safety violations:', violRes.status, 'Count:', violData.data ? violData.data.length : 'none');

    // 5. Incidents
    const incRes = await fetch('http://localhost:8080/api/incidents', { headers: { 'Cookie': cookie } });
    const incData = await incRes.json();
    console.log('5. Incidents:', incRes.status, 'Count:', incData.data ? incData.data.length : 'none');

    // 6. Reports Preview
    const repProj = await fetch('http://localhost:8080/api/reports/preview?type=projects', { headers: { 'Cookie': cookie } });
    const repProjData = await repProj.json();
    console.log('6. Report preview (projects):', repProj.status, 'Title:', repProjData.data?.reportTitle, 'Rows:', repProjData.data?.rows?.length);

    const repInv = await fetch('http://localhost:8080/api/reports/preview?type=inventory', { headers: { 'Cookie': cookie } });
    const repInvData = await repInv.json();
    console.log('   Report preview (inventory):', repInv.status, 'Title:', repInvData.data?.reportTitle, 'Rows:', repInvData.data?.rows?.length);

    // 7. CSV Export
    const csvRes = await fetch('http://localhost:8080/api/reports/export/csv?type=projects', { headers: { 'Cookie': cookie } });
    const csvText = await csvRes.text();
    console.log('7. CSV Export:', csvRes.status, 'Content-Type:', csvRes.headers.get('content-type'), 'First line:', csvText.split('\n')[0]);
  } catch (err) {
    console.error('Error during verification:', err);
  }
}
verifyAll();
