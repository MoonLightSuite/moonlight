graph [
  directed 1
  node [
    id 0
    label "W445439334"
      predicates "339085844"
      predicates "TRANSPORTBUSSTOP"
      predicates "poi"
      predicates "main_square"
  ]
  node [
    id 1
    label "N1497364745"
      predicates "1497364745"
      predicates "FOODRESTAURANT"
      predicates "poi"
      predicates "taxi"
  ]
  node [
    id 2
    label "N3530332741"
      predicates "3530332741"
      predicates "HEALTHHOSPITAL"
      predicates "poi"
  ]
  node [
    id 3
    label "N2262773429"
      predicates "2262773429"
      predicates "TRANSPORTBUSSTOP"
      predicates "poi"
      predicates "taxi"
      predicates "taxi"
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
