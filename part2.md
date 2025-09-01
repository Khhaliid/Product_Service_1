---
marp: true
---

# Molnbaserad E-commerce Integration - Del 2

## Projektöversikt

Skapa en molnbaserad integrationslösning bestående av **tre separata tjänster** som kommunicerar via både synkron och asynkron kommunikation. Systemet ska vara deployat på Microsoft Azure med automatiserad CI/CD och omfattande monitoring.

## Systemets tre delar

---

### Del 2: Product Service

**Ansvar och funktionalitet:**

- Produktkataloghantering (CRUD-operationer)
- Inventory och lagerhantering med tillgänglighetskontroll
- Produktkategorisering och sökfunktionalitet
- Produktbilder och filhantering
- Prissättning och produktattribut
- Ska gå att göra en order (valfritt med egen service/eget repo)

---

**Teknisk implementation:**

- Spring Boot applikation med REST API
- Azure SQL Database för produktdata
- Azure Blob Storage för produktbilder och filer
- Search och filtering funktionalitet
- Inventory tracking med concurrency handling

---

### Del 3: Order Service (VG-nivå) eller Integration Layer (Godkänt-nivå)

**För Godkänt - Integration Layer:**

- Koordinering mellan User Service och Product Service
- Enkel shopping cart funktionalitet
- Basic order processing utan avancerad logik
- Event handling för user-product interactions

---

**För VG-nivå - Full Order Service:**

- Komplett beställningshantering med komplex business logic (Separat Order Service)
- Order validation mot User Service och Product Service
- Betalningsprocesser och order status management (fejkad)
- Order history och customer analytics

---

### Bonusuppgift

- Skapa en React frontend som kan prata med Product Service (Kika på tidigare bonus lektioner om frontend).
- Helt fritt val av teknik och design.
- Ni får lov att Vibe-koda en frontend med ChatGPT, Clade mm.
- Det underlättar om ni har Swagger UI för att testa API:erna och där ni kan skicka med den informationen till AI.

---

## Uppgift

- Skapa ett fristående Spring Boot projekt som innehåller Product Service i ett eget GitHub-repo.
- Demo av kod och funktionalitet måndag 8/9 kl 9:00.
