$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/users/login" -Headers @{"Content-Type"="application/json"} -Body '{"username":"strager","password":"password"}'
$token = $response.token
Write-Host "Token: $token"

$chars = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/characters" -Headers @{"Authorization"="Bearer $token"}
Write-Host "Characters: "
$chars | ConvertTo-Json -Depth 5
