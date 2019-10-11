time = 0:1:200;
seqS = time;
seqT = time';

psi1 = 'a /\ b';

%a >= -30
psi1_Pred(1).str = 'a';
psi1_Pred(1).A   =  -1;
psi1_Pred(1).b   =  30;

%a <= 30
psi1_Pred(2).str = 'b';
psi1_Pred(2).A   =  1;
psi1_Pred(2).b   =  30;

rob1 = fw_taliro(psi1,psi1_Pred,seqS,seqT);

psi2 = '(a /\ !b) \/ (b /\ c)';

%a >= -30
psi2_Pred(1).str = 'a';
psi2_Pred(1).A   =  -1;
psi2_Pred(1).b   =  30;

%a >= 0
psi2_Pred(2).str = 'b';
psi2_Pred(2).A   =  -1;
psi2_Pred(2).b   =  0;

%a <= 30
psi2_Pred(3).str = 'c';
psi2_Pred(3).A   =  1;
psi2_Pred(3).b   =  30;

rob2 = fw_taliro(psi2,psi2_Pred,seqS,seqT);

psi3 = '[] a';

%a >= 5
psi3_Pred(1).str = 'a';
psi3_Pred(1).A   =  -1;
psi3_Pred(1).b   =  -5;

rob3 = fw_taliro(psi3,psi3_Pred,seqS,seqT);


psi4 = '[] ((a /\ b) \/ c)';

%a >= 5
psi4_Pred(1).str = 'a';
psi4_Pred(1).A   =  -1;
psi4_Pred(1).b   =  -5;

%a <= 9
psi4_Pred(2).str = 'b';
psi4_Pred(2).A   =  1;
psi4_Pred(2).b   =  9;

%a >= 7
psi4_Pred(3).str = 'c';
psi4_Pred(3).A   =  -1;
psi4_Pred(3).b   =  -7;

rob4 = fw_taliro(psi4,psi4_Pred,seqS,seqT);


psi5 = '[]_[0,3] a';

%a >= 5
psi5_Pred(1).str = 'a';
psi5_Pred(1).A   =  -1;
psi5_Pred(1).b   =  -5;


rob5 = fw_taliro(psi5,psi5_Pred,seqS,seqT);


psi6 = '([]_[0,2] a) /\ ([]_[0,3] a)';

%a >= 5
psi6_Pred(1).str = 'a';
psi6_Pred(1).A   =  -1;
psi6_Pred(1).b   =  -5;


rob6 = fw_taliro(psi6,psi6_Pred,seqS,seqT);

psi7 = '[] (a /\ !a)';

%a >= 5
psi7_Pred(1).str = 'a';
psi7_Pred(1).A   =  -1;
psi7_Pred(1).b   =  -5;


rob7 = fw_taliro(psi7,psi7_Pred,seqS,seqT);


psi8 = '!((<> (a /\ b)) \/ <> (!a \/ !b))';

%a >= -30
psi8_Pred(1).str = 'a';
psi8_Pred(1).A   =  -1;
psi8_Pred(1).b   =  30;

%a <= 30
psi8_Pred(2).str = 'b';
psi8_Pred(2).A   =  1;
psi8_Pred(2).b   =  30;

rob8 = fw_taliro(psi8,psi8_Pred,seqS,seqT);


psi9 = '<> ((a /\ b) \/ c)';

%a >= -10
psi9_Pred(1).str = 'a';
psi9_Pred(1).A   =  -1;
psi9_Pred(1).b   =  10;


%a <= 60
psi9_Pred(2).str = 'b';
psi9_Pred(2).A   =  1;
psi9_Pred(2).b   =  60;

%a >= 55
psi9_Pred(3).str = 'c';
psi9_Pred(3).A   =  -1;
psi9_Pred(3).b   =  -55;

rob9 = fw_taliro(psi9,psi9_Pred,seqS,seqT);


psi10 = '<> (a)';

%a >= -10
psi10_Pred(1).str = 'a';
psi10_Pred(1).A   =  -1;
psi10_Pred(1).b   =  10;


rob10 = fw_taliro(psi10,psi10_Pred,seqS,seqT);



%figure; BrTrace.PlotRobustSat('ev (a[t]>=-10 and a[t]<=60) or (a[t]>=55)',1);
%figure; BrTrace.PlotRobustSat('ev (a[t]>=-10)',1);






