# TechLense integration architecture

TechLense is an orchestration layer. Vendor-specific code sits behind interchangeable connectors.

## Shop management
Primary target: **Tekmetric**.

Responsibilities: active RO/vehicle context, inspection findings, estimate preparation, approval events and completed technician notes. TechLense should use authorized APIs/webhooks where available and must not store a user's Tekmetric password.

## Scan tools
First target: **Autel**.

Responsibilities: VIN, pre/post scans, DTCs, freeze-frame and selected live PIDs. The exact adapter depends on supported Autel hardware/cloud interfaces.

## Repair information
Targets include **Identifix, ProDemand and ALLDATA**, depending on the shop's licensed provider.

Responsibilities: TSBs, diagnostic procedures, repair procedures, wiring and specifications. Content is only tagged VERIFIED when it came from an authorized repair-information source. No scraping or redistribution of licensed content.

## Parts
Primary path: **Tekmetric -> NexPart** when the shop uses Tekmetric's NexPart integration.

Responsibilities: supplier results, shop cost, availability and ordering. TechLense may prepare an order, but v0.1 requires explicit human confirmation before submission.

## Closed-loop workflow
RO loaded -> pre-scan -> repair-info research -> guided diagnosis -> document failure -> prepare estimate -> customer approval -> order parts -> guided repair -> post-scan -> confirmed fix -> close RO.

## Security
Credentials and API secrets must not be committed to GitHub or embedded in the APK. Production connectors should use vendor-authorized authentication and a secure server-side credential layer.
