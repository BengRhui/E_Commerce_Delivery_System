# SwiftCart E-Commerce Warehouse and Delivery System   

A multi-threaded Java backend application simulating a highly automated e-commerce fulfillment hub. This system models real-time, simultaneous warehouse operations from robotic order picking to transport dispatch while strictly enforcing thread safety, resource synchronization, and memory management.  

---

## System Architecture
The project is built entirely in Java, bypassing basic threading in favor of robust, advanced concurrency constructs to prevent race conditions, thread starvation, and deadlocks.  
* Language: Java
* Design Paradigm: Producer-Consumer Model

---

## Prerequisites for Execution
* Java Development Kit (JDK) 8 or higher

---

## Operational Pipeline
The simulation mimics physical warehouse constraints through precisely controlled thread execution, with features including:
* **Order Intake & Generation**
  * Simulates order influx based on configurable stress levels (Low: 100, Normal: 600, High: 1000 orders).
  * Features an automated retry mechanism for orders rejected due to missing details.
* **Picking Station**
  * 4 robotic arms (governed by a 4-permit Semaphore) safely decrement shared inventory levels utilizing `ConcurrentHashMap` atomic updates.
* **Packing & Labelling**
  * Validated items are packed and passed through scanner threads using `LinkedBlockingQueue` structures to ensure safe cross-thread data handoffs.
* **Sorting & Batching**
  * Parcels are categorized into 5 regional zones (e.g., East Malaysia, Northern) and consolidated into shipping containers holding up to 30 boxes.
* **Logistics Dispatch**
  * 3 Automated Guided Vehicles (AGVs) managed by an ExecutorService thread pool route containers to 2 loading bays.
  * Trucks load up to 18 containers before departing.  

---

## Advanced System Dynamics
* **Fault Injection**
  * The system realistically models a 1% probability of operational errors (e.g., mislabeling, picking failures) and a 5% AGV breakdown rate that triggers a fixed 3-second thread stall for repairs.  
* **Dynamic Resource Contention**
  * If the loading bays reach their 5-container maximum capacity, packing threads are dynamically suspended and resumed once space clears.  
* **Graceful Termination**
  * Implements a strict shutdown protocol using `join()` and `awaitTermination()` to ensure all executing threads complete their lifecycles cleanly without dangling objects or memory leaks.  
