# Entrega y evaluacion de calidad en GitLab Quantum

Esta guia aplica el **Manual del estudiante – Evaluacion de Calidad en GitLab** al repositorio Cotizador Leguia. La configuracion local deja el proyecto preparado; la nota y el estado de aprobado solo se conocen despues de ejecutar el pipeline real en GitLab Quantum.

## Requisitos del manual y estado local

| Requisito | Estado |
| --- | --- |
| `.gitlab-ci.yml` en la raiz con el include oficial `root/laboratorio-quality` | Configurado |
| `project.yml` en la raiz con datos del proyecto y evaluacion | Configurado con los datos confirmados por el estudiante |
| `.gitignore` para entornos, dependencias, compilados y secretos | Configurado; incluye `node_modules/`, `venv/`, `vendor/`, `build/`, `.env` y archivos locales |
| Crear cuenta y proyecto vacio en GitLab Quantum | Accion del estudiante en la web |
| Conectar GitLab como remoto adicional y hacer push | Pendiente de la URL HTTPS exacta del proyecto creado |
| Revisar las etapas `detect`, `quality` y `notify` | Pendiente del primer pipeline real |

## Datos academicos configurados

`project.yml` registra el curso **Taller de Investigacion II**, la evaluacion con ponderacion de **100 %**, al docente **Maglioni Arana Caparachin** y al estudiante **Piero Pool Chaupis Leguia**. El codigo `75177414` se tomo del correo institucional proporcionado. Verifica esos datos antes de entregar si el docente o el curso indican otro formato.

## Subir a GitLab sin reemplazar GitHub

Este directorio ya tiene historial y el remoto `origin` de GitHub. **No ejecutes `rm -rf .git`, `git init` ni reemplaces `origin`**: eso desconectaria la copia de GitHub y no es necesario para entregar en una segunda plataforma.

1. Entra a `https://gitlab.quantumdevops.online` y crea/inicia tu cuenta. El manual pide usar el codigo de estudiante como usuario y el correo institucional.
2. Crea un proyecto en blanco. Desmarca la inicializacion con README para mantener el primer push sencillo.
3. Copia la URL HTTPS que GitLab muestra para ese proyecto.
4. Desde PowerShell, en la raiz de este repositorio, agrega GitLab como remoto adicional y envia la rama `main`:

```powershell
git remote add gitlab <URL-HTTPS-DEL-PROYECTO>
git push -u gitlab main
```

Si ya agregaste el remoto `gitlab`, actualiza su URL con `git remote set-url gitlab <URL-HTTPS-DEL-PROYECTO>` en vez de agregarlo otra vez. El inicio de sesion debe pedir tus credenciales; pega un token vigente solo en el cuadro de contrasena del gestor de credenciales. No lo pongas en la URL, comandos, archivos ni capturas.

El token de acceso que se compartio anteriormente en la conversacion debe revocarse y reemplazarse por uno nuevo con expiracion y solo los permisos que indica el manual (`read_repository` y `write_repository`). No se incluye ningun token en el repositorio.

## Revisar el pipeline

Despues del push, abre el proyecto en GitLab Quantum y ve a **Build > Pipelines**. Abre la ejecucion mas reciente y revisa las etapas `detect`, `quality` y `notify`:

- `passed` indica que la ejecucion termino y se envio la notificacion; no garantiza una nota alta.
- `running` sigue procesando; `pending` esta en cola.
- Si falla, abre la etapa roja y conserva el mensaje completo del log para diagnosticarlo.

La configuracion incluye una plantilla de pipeline mantenida por el curso. Este repositorio no contiene esa plantilla y no puede confirmar por adelantado su resultado, los hallazgos de Sonar ni el correo de notificacion.

## Reglas de integridad de la evaluacion

- No excluyas carpetas de codigo (`app/`, `backend/`, `src/` ni equivalentes) para mejorar artificialmente el puntaje. El manual penaliza estas exclusiones.
- No agregues `node_modules`, entornos virtuales, `vendor`, `.git`, carpetas `build`, secretos ni archivos `.env` al commit.
- No inventes ni rellenes resultados de calidad. Registra la nota y hallazgos que aparezcan realmente en el pipeline.
- El archivo de cobertura JaCoCo versionado es una medicion anterior y no se debe presentar como cobertura vigente hasta regenerarlo para el commit evaluado.

## Comprobaciones locales antes del push

Desde la raiz del proyecto:

```powershell
git status --short --branch
git check-ignore backend/api/.env backend/api/.venv/ app/build/
git diff --check
```

Confirma que `.env`, entornos y compilados salen como ignorados, que no hay claves en los cambios y que el arbol de trabajo solo contiene los cambios que quieres entregar.
