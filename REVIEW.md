# Design Review – KnightPathJR

## 1. Progetto e organizzazione

Ho realizzato un'applicazione Java standalone eseguibile interamente tramite Docker.  
La classe principale (`KnightPathJR`) contiene un metodo `public static void main(String[] args)` e una classe statica interna `Position` per rappresentare le coordinate del cavallo.

L'obiettivo è garantire la massima portabilità ed evitare dipendenze sull’ambiente locale.

- Utilizzo di `Dockerfile` e `build.sh` per la compilazione automatica
- Struttura ordinata del progetto: `src/`, `lib/`, `out/`
- Inclusi `README.md` e `.gitignore` per documentazione e gestione Git

---

## 2. Parsing JSON e configurazione

- Uso della libreria `org.json` (via JAR) per semplicità e leggerezza
- Le URL di board e comandi sono fornite come variabili d’ambiente
- Parsing sicuro e logging per il tracciamento dei valori

---

## 3. Logica dei comandi

### Parsing Board
- Il JSON della board contiene un array di ostacoli
- Gli ostacoli sono memorizzati in un `Set<Position>` per:
  - Evitare duplicati
  - Avere lookup `O(1)`
  - Disinteresse per l’ordinamento
- `Position` implementa `equals()` e `hashCode()` per funzionare correttamente nel `Set`

### Parsing Comandi
- I comandi sono letti da un `JSONArray`, trattato come lista indicizzata
- Non ho usato `Stream` per mantenere un ciclo `for` più leggibile e controllabile

### Gestione dei comandi
- Il parsing si basa su `command.startsWith()` poiché i comandi possibili sono solo tre: `START`, `ROTATE`, `MOVE`
- Ogni comando ha un metodo dedicato:
  - `handleStart` → imposta la posizione iniziale e valida
  - `handleRotate` → aggiorna la direzione
  - `handleMove` → muove il cavallo passo-passo. E' necessario perchè la nuova posizione N passi più avanti potrebbe essere valida ma il cavallo aver superato un ostacolo. 
    - Se incontra un ostacolo, si ferma **prima**
    - Se esce dalla board, termina con errore

---

## 4. Robustezza e logging

- Ogni passo del flusso è loggato su console: lettura URL, parsing, rotazioni, movimenti
- I log facilitano il debug anche in ambienti runtime non interattivi (es. Docker)

---

## 5. Testing – Strategia e Priorità

Anche se il progetto non richiede test completi, ecco come affronterei il testing in modo efficace:

### Priorità: **logica dei comandi**
1. `handleStart`
   - Posizione valida: deve partire
   - Su ostacolo o fuori board : deve restituire `INVALID_START_POSITION` e fermare l'esecuzione

2. `handleMove`
   - Movimento semplice entro i limiti
   - Movimento che supera i bordi: deve restituire `OUT_OF_THE_BOARD` e fermare l'esecuzione
   - Movimento che incontra ostacolo : si ferma prima
 
3. `handleRotate`
   - Aggiorna la direzione correttamente

---

### Tecniche consigliate

#### 1. **Unit test** con JUnit
- Isolare `handleStart`, `handleMove`, `Position.move()` e testarli con input mirati

#### 2. **Mocking JSON input**
- Usare JSON hardcoded nei test (senza chiamate HTTP reali)

#### 3. **Test end-to-end (E2E) da Docker**
- Verificare il risultato JSON a partire da un `docker run`
- Controllare lo `stdout` per logging e risultato finale

---

### Sintesi logica test 

> Mi concentro prima su test funzionali della logica, poi su casi limite.  
> Evito over-testing su parsing o strutture statiche, che sono già ben definite.
