graph [
  directed 1
  node [
    id 0
    label "0"
    predicates ['p', 'o', 'i']
    people 201
  ]
  node [
    id 1
    label "1"
    predicates ['p', 'o', 'i', 'taxi']
  ]
  node [
    id 2
    label "2"
    predicates ['p', 'o', 'i']
    people 100
  ]
  node [
    id 3
    label "3"
    predicates ['p', 'o', 'i', 'taxi']
  ]
  edge [
    source 0
    target 1
    times 3.0
  ]
  edge [
    source 0
    target 3
    times 7.0
  ]
  edge [
    source 1
    target 0
    times 3.0
  ]
  edge [
    source 1
    target 2
    times 5.0
  ]
  edge [
    source 2
    target 1
    times 5.0
  ]
  edge [
    source 2
    target 3
    times 4.0
  ]
  edge [
    source 3
    target 0
    times 7.0
  ]
  edge [
    source 3
    target 2
    times 4.0
  ]
]
