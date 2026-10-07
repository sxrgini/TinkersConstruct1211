"""Rewrites 1.20 style recipe builders (Consumer<FinishedRecipe>) to the 1.21 RecipeOutput style."""
import re,sys,glob,os
ROOT="src/main/java/slimeknights/tconstruct/"

def split_args(s):
    args=[];depth=0;cur=""
    i=0
    instr=False
    while i<len(s):
        c=s[i]
        if c=='"' and (i==0 or s[i-1]!='\\'): instr=not instr
        if not instr:
            if c in "(<[{": depth+=1
            elif c in ")>]}": depth-=1
            elif c=="," and depth==0:
                args.append(cur.strip());cur="";i+=1;continue
        cur+=c;i+=1
    if cur.strip(): args.append(cur.strip())
    return args

def match_paren(s,start):
    # s[start]=='('
    depth=0;i=start;instr=False
    while i<len(s):
        c=s[i]
        if c=='"' and s[i-1]!='\\': instr=not instr
        if not instr:
            if c=='(': depth+=1
            elif c==')':
                depth-=1
                if depth==0: return i
        i+=1
    return -1

def drop_id_arg(expr):
    """expr is `new Foo(args)`; removes a top-level arg that is exactly `id`"""
    m=re.match(r"new\s+([\w.<>]+)\(",expr)
    if not m: return expr
    st=expr.index("(")
    en=match_paren(expr,st)
    if en!=len(expr)-1: return expr
    args=split_args(expr[st+1:en])
    args=[a for a in args if a!="id"]
    return expr[:st+1]+", ".join(args)+")"

def transform(s):
    s=s.replace("Consumer<FinishedRecipe>","RecipeOutput")
    s=s.replace("import net.minecraft.data.recipes.FinishedRecipe;","import net.minecraft.data.recipes.RecipeOutput;")
    # accept wrappers
    out="";pos=0
    key="new LoadableFinishedRecipe<>("
    while True:
        i=s.find(key,pos)
        if i<0:
            out+=s[pos:];break
        # find "<var>.accept(" before
        j=s.rfind(".accept(",0,i)
        if j<0 or i-j>len(".accept(")+2:
            out+=s[pos:i+len(key)];pos=i+len(key);continue
        k=i+len(key)-1
        e=match_paren(s,k)
        args=split_args(s[k+1:e])
        # after the wrapper there is a closing ) of accept
        recipe=drop_id_arg(args[0])
        adv=args[2] if len(args)>2 else "null"
        var=s[:j].split()[-1].split("(")[-1] if False else None
        # variable name: text between previous whitespace/start and .accept
        m=re.search(r"([\w.]+)$",s[pos:j])
        # find the receiver
        recv_start=j
        while recv_start>0 and (s[recv_start-1].isalnum() or s[recv_start-1] in "_."): recv_start-=1
        recv=s[recv_start:j]
        out+=s[pos:recv_start]+recv+".accept(id, "+recipe+", "+adv+")"
        # skip closing paren of accept
        after=e+1
        assert s[after]==')',(s[after-40:after+10])
        pos=after+1
    s=out
    return s

for F in sys.argv[1:]:
    s=open(F).read();o=s
    s=transform(s)
    if s!=o:
        open(F,"w").write(s);print("rewrote",F)
