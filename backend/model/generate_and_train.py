"""Generate synthetic-only quote data and train an interpretable conversion baseline."""
import csv, json, random
from pathlib import Path
import joblib
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score, confusion_matrix
ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "qa"
def generate():
    rng = random.Random(742)
    services = ["Carpas", "Toldos", "Tapizado"]
    rows = []
    for i in range(1, 201):
        complete = int(rng.random() < .72)
        reviewed = int(complete and rng.random() < .78)
        amount = round(rng.uniform(150, 8500), 2)
        discount = round(rng.uniform(0, 20), 2)
        quote_min, days_due, response = rng.randint(2,75), rng.randint(1,90), rng.randint(0,20)
        z = 1.3 + .8*complete + .65*reviewed - .00022*amount - .035*discount - .012*quote_min - .018*response - .004*days_due
        converted = int(rng.random() < 1/(1+np.exp(-z)))
        rows.append({"quote_id":f"SYN-{i:03d}","service_type":services[(i-1)%3],"quote_amount":amount,"discount_percentage":discount,"requirement_complete":complete,"ai_interpretation_reviewed":reviewed,"quotation_time_minutes":quote_min,"days_to_requested_date":days_due,"customer_response_days":response,"status":"CONCRETADA" if converted else "NO_CONCRETADA","converted_to_sale":converted,"synthetic_flag":"true"})
    path=OUT/"data"/"synthetic_quotes_200.csv"; path.parent.mkdir(parents=True,exist_ok=True)
    with path.open("w",newline="",encoding="utf-8") as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
def train():
    data=pd.read_csv(OUT/"data"/"synthetic_quotes_200.csv")
    y=data.converted_to_sale.astype(int).to_numpy()
    cols=["service_type","quote_amount","discount_percentage","requirement_complete","ai_interpretation_reviewed","quotation_time_minutes","days_to_requested_date"]
    X=data[cols]; categorical=["service_type"]; numeric=cols[1:]
    all_idx=np.arange(len(data)); train,remain=train_test_split(all_idx,test_size=.30,random_state=742,stratify=y); validation,test=train_test_split(remain,test_size=.50,random_state=742,stratify=y[remain])
    pipeline=Pipeline([("features",ColumnTransformer([("category",OneHotEncoder(handle_unknown="ignore"),categorical),("numeric",StandardScaler(),numeric)])),("logistic",LogisticRegression(max_iter=1000,random_state=742))])
    pipeline.fit(X.iloc[train],y[train])
    def evaluate(indices):
        probabilities=pipeline.predict_proba(X.iloc[indices])[:,1]; predicted=(probabilities>=.5).astype(int)
        return {"accuracy":float(accuracy_score(y[indices],predicted)),"precision":float(precision_score(y[indices],predicted,zero_division=0)),"recall":float(recall_score(y[indices],predicted,zero_division=0)),"f1":float(f1_score(y[indices],predicted,zero_division=0)),"roc_auc":float(roc_auc_score(y[indices],probabilities))}
    probabilities=pipeline.predict_proba(X.iloc[test])[:,1]; predicted=(probabilities>=.5).astype(int)
    cm=confusion_matrix(y[test],predicted,labels=[0,1]); metrics={"dataset":"synthetic_quotes_200.csv","synthetic_only":True,"seed":742,"split_counts":{"train":len(train),"validation":len(validation),"test":len(test)},"validation_metrics":evaluate(validation),"test_metrics":evaluate(test),"threshold":.5,"note":"Synthetic technical validation only; not Leguía field or pretest/posttest evidence."}
    out=OUT/"prediction";out.mkdir(parents=True,exist_ok=True)
    (out/"metrics.json").write_text(json.dumps(metrics,indent=2)+"\n",encoding="utf-8")
    (out/"model_metadata.json").write_text(json.dumps({"model":"LogisticRegression","version":"synthetic-logreg-1.0","features":cols,"seed":742,"split_counts":metrics["split_counts"],"status":"TRAINED_ON_SYNTHETIC_DATA_ONLY"},indent=2)+"\n",encoding="utf-8")
    with (out/"confusion_matrix.csv").open("w",newline="",encoding="utf-8") as f:
        w=csv.writer(f);w.writerow(["actual\\predicted","NO_CONVERSION","CONVERSION"]);w.writerow(["NO_CONVERSION",*cm[0]]);w.writerow(["CONVERSION",*cm[1]])
    joblib.dump(pipeline,out/"logistic_pipeline.joblib")
    print(json.dumps(metrics,indent=2))
if __name__ == "__main__":
    generate(); train()
