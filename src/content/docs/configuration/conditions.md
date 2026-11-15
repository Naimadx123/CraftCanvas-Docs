---
title: Conditions
description: Show, disable or restrict a component based on permissions, values and items.
---

Conditions decide whether a component is visible, whether it can be clicked and whether its actions may run. Add them under a component's `conditions` map.

| Setting | What the player sees |
| --- | --- |
| `visible` | If false, the component is hidden |
| `enabled` | If false, the component stays visible but cannot be used |
| `execute` | Checks whether the action may run when clicked |

For a permission-only button, this is enough:

```yaml
conditions:
  enabled:
    permission: server.travel
```

Players without `server.travel` can see the button, but cannot use it. Add a `disabled` appearance if you want a different color or tooltip. Use `visible` instead when they should not see the component at all.

Each condition contains one check. To combine several checks, use `all` for all of them, `any` for at least one, or `not` to reverse a check.

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

This checks three emeralds in storage inventory and a balance of 25 coins. It does not take the emeralds or charge the coins. Use [item/economy actions](/configuration/actions/#confirm-an-economy-purchase) for payment.

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

## All condition types

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


Comparison operators are `eq` (equal), `ne` (not equal), `gt` (greater), `ge` (greater or equal), `lt` (less), `le` (less or equal) and `contains`. Numeric strings compare as numbers when both sides are finite numbers. Ordered comparisons need valid numbers. `contains` checks text and is case-sensitive.

Conditions are checked again when the player activates the component. A required integration or callback that is unavailable makes the check fail. Item checks count matching storage-inventory items, excluding armor and offhand, and ignore stack amount when comparing the item itself. Checking a resource exists on the server does not prove the player has loaded its pack.
