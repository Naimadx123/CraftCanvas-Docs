---
title: "Conditions"
---

A condition is a map containing exactly one operator. Conditions on visibility, enabled state and execution are separate. Action conditions and the relevant component conditions are checked again at activation. Missing integrations, callbacks or unresolved values fail closed.

| Operator | Value shape | Behavior |
| --- | --- | --- |
| permission | permission string, or `{value: string}` | Player permission |
| world | world name, or `{value: string}` | Exact world name |
| integration | provider/plugin name, or `{value: string}` | Ready native/vanilla/custom provider, PlaceholderAPI or Vault economy |
| variable-equals | `{name: string, value: scalar}` | Variable comparison |
| variable-type | `{name: string, value: type}` | string/integer/number/boolean check |
| compare | `{left: scalar, operator: string, right: scalar}` | Literal or exact variable placeholder comparison |
| session | `{name: string, value: scalar}` | selected, page, tab, busy or screen |
| workbench | ready or holding | Workbench readiness or held stack; requires a workbench definition |
| asset | `{asset: resource-reference}` | Server resource resolution, not proof of client pixels |
| item | `{asset: item-reference, amount: positive-integer}` | Matching storage-inventory item count |
| balance | `{amount: positive-number}` | Available Vault economy balance |
| placeholder | `{value: formatted-string, operator: string, expected: scalar}` | Compare the rendered literal placeholder result |
| callback | `{id: callback-id, data: optional-map}` | Registered server API condition |
| all | nonempty condition list | Every child succeeds |
| any | nonempty condition list | At least one child succeeds |
| not | one nested condition | Inversion |

Comparison operators are eq, ne, gt, ge, lt, le, contains. Numeric strings are compared numerically when both operands are finite numbers. Otherwise equality compares their scalar/string forms. Ordered comparisons reject invalid numbers. `contains` is a case-sensitive string operation. No script, regular-expression execution or arbitrary expression evaluator exists.

Declare a player-scoped integer `level` for this example:

```yaml
conditions:
  enabled:
    all:
      - permission: craftcanvas.select
      - compare: {left: '{player.level}', operator: ge, right: 10}
  execute:
    not:
      session: {name: busy, value: true}
```

Item conditions use the official provider-built ItemStack and `isSimilar`, ignoring amount for the comparison. They count storage slots, not armor/offhand. Balance conditions require a registered economy provider. Placeholder values remain literal text.

## Hide, disable or guard execution

Put this map on a component. The button is visible in the overworld, enabled for permitted players and executable only when no action is busy:

```yaml
conditions:
  visible: {world: world}
  enabled:
    any:
      - permission: server.travel
      - permission: server.vip
  execute:
    not: {session: {name: busy, value: true}}
```

Hidden components do not draw. Disabled components can still show a tooltip but have no action ticket. An action-level condition guards only that action; it is independent of the component's enabled condition.

## Compare typed variables

Declare `level` as an integer and `route` as a string. Use exact variable placeholders for numeric comparisons:

```yaml
conditions:
  enabled:
    all:
      - variable-type: {name: level, value: integer}
      - compare: {left: '{level}', operator: ge, right: 10}
      - variable-equals: {name: route, value: north}
```

Use `operator: contains` for case-sensitive text matching. `not` takes one nested condition; `all` and `any` take lists.

## Require resources or items

```yaml
conditions:
  enabled:
    all:
      - integration: vault
      - balance: {amount: 25}
      - item:
          asset: {provider: vanilla, kind: item, id: EMERALD}
          amount: 3
```

This checks three emeralds in storage inventory and an economy balance of 25. It does not consume either. Use [item/economy actions](/configuration/actions/#confirm-an-economy-purchase) for payment.

To check an asset without requiring the player to own it, use:

```yaml
conditions:
  visible:
    asset:
      asset: {provider: nexo, kind: item, id: travel_ticket}
```

Replace `travel_ticket` with an installed provider ID. `integration: nexo` checks provider readiness instead of one ID. A resource condition confirms server resolution, while resource-pack status controls whether the player can open the screen.

## PlaceholderAPI or addon logic

```yaml
conditions:
  enabled:
    all:
      - integration: placeholderapi
      - placeholder: {value: '%player_level%', operator: ge, expected: 10}
```

The relevant PlaceholderAPI expansion must be installed. Values are treated as literal results. For a server-side check implemented by your addon, use `callback: {id: 'travel:ready'}`; see [registering conditions](/developers/callbacks/#register-an-action-and-condition).
