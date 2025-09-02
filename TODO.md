# TODO-lista: Molnbaserad E-commerce Integration

## Del 2: Product Service

### 1. Projektstruktur & Setup
- [x] Skapa ett nytt Spring Boot-projekt (Maven/Gradle) för Product Service
- [x] Lägg till nödvändiga dependencies:
- [x] Spring Web
- [x] Spring Data JPA
- [x] Spring Security (om autentisering behövs)

### 2. Datamodell & Databas
- [ ] Definiera entiteter: `Product`, `Category`, `Inventory`, `ProductImage`, `ProductAttribute`
- [x] Implementera JPA repositories för CRUD-operationer

### 3. REST API
- [x] Implementera CRUD-endpoints för produkter
- [x] Implementera endpoints för produktkategorier
- [ ] Implementera endpoints för produktbilder (upload/download till Azure Blob Storage) // TODO: Fråga Lars
- [x] Implementera endpoints för produktattribut och prissättning
- [x] Säkerställ korrekt HTTP-statushantering och felhantering

### 4. Inventory & Concurrency
- [ ] Implementera lagerhantering med fält som `stockQuantity`
- [ ] Implementera endpoint för att kolla tillgänglighet
- [ ] Hantera concurrency (optimistic locking / versionering) vid uppdatering av lager

### 5. Search & Filter
- [ ] Implementera sökfunktionalitet på [x] namn, [x] kategori, [ ] attribut
- [ ] Implementera filtrering (t.ex. price range, category, availability)

### 6. Filhantering 
 // TODO: Fråga Lars
- [ ] Implementera endpoints för upload och delete av filer
- [ ] Säkerställ åtkomstkontroll och säker URL-generering

### 7. Testning
- [ ] Unit tests för services och repositories

---

## Övrigt / DevOps & Monitoring
- [x] Dokumentera API:er med Swagger/OpenAPI
