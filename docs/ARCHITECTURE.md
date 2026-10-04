# MekView architecture

## v0.1 flow
Ray-Ban Meta Gen 2 -> Meta Wearables DAT -> Pixel app -> vehicle session -> AI service boundary -> sourced response -> audio/UI.

## Trust model
Every useful answer is classified as:
- **Verified repair information** — exact information supplied from an authorized repair-information source.
- **AI diagnostic suggestion** — reasoning/hypothesis that must be verified by the technician.
- **Technician finding** — observation entered or captured by the technician.

Never promote an AI-generated torque value, fluid specification, wiring value, or repair procedure to Verified.

## Identifix
v0.1 deliberately contains no scraping or automated extraction. A technician may load relevant information they are authorized to use. Commercial integration requires appropriate permission/licensing.

## Hardware milestones
1. Connect Pixel to Ray-Ban Meta Gen 2.
2. Capture POV frame/photo.
3. Send selected visual context to the AI service.
4. Return concise spoken guidance.
5. Add inspection photo + voice findings.
6. Add scan-tool/VCI data adapter.
