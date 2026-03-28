import { createTheme } from "@mui/material/styles";

export const appTheme = createTheme({
  palette: {
    primary: {
      main: "#9c4f2f"
    },
    secondary: {
      main: "#285a55"
    },
    background: {
      default: "#f4f1ea",
      paper: "#fffaf2"
    }
  },
  shape: {
    borderRadius: 18
  },
  typography: {
    h2: {
      fontSize: "clamp(2.2rem, 5vw, 4.4rem)",
      fontWeight: 700,
      lineHeight: 1
    },
    h6: {
      lineHeight: 1.45
    }
  }
});
