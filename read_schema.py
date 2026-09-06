import urllib.request
import json

url = "https://ecwueqoxfjcepktubqrs.supabase.co/rest/v1/?apikey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVjd3VlcW94ZmpjZXBrdHVicXJzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIyMzcxOTIsImV4cCI6MjA5NzgxMzE5Mn0.2i8tPlBlNKZO1rCo2KFMXG78iZyt40EGm6_R5koDJ08"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode('utf-8'))

definitions = data.get('definitions', {})
if 'vitals_hourly' in definitions:
    print(json.dumps(definitions['vitals_hourly'], indent=2))
else:
    print("Not found")
