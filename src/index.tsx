import React from 'react';
import { NativeModules, processColor } from 'react-native';

import UIMenuView from './UIMenuView';
import type {
  MenuComponentProps,
  MenuAction,
  ProcessedMenuAction,
  MenuHeader,
} from './types';

function processAction(action: MenuAction): ProcessedMenuAction {
  return {
    ...action,
    imageColor: processColor(action.imageColor),
    titleColor: processColor(action.titleColor),
    subactions: action.subactions?.map((subAction) => processAction(subAction)),
  };
}

const MenuView: React.FC<MenuComponentProps> = ({ actions, ...props }) => {
  const processedActions = actions.map<ProcessedMenuAction>((action) =>
    processAction(action)
  );
  return <UIMenuView {...props} actions={processedActions} />;
};

const showMenu = (
  actions: MenuAction[],
  headerConfig?: MenuHeader
): Promise<string> => {
  const processedActions = actions.map<ProcessedMenuAction>((action) =>
    processAction(action)
  );
  return NativeModules.MenuModule.showMenu(processedActions, {
    image: headerConfig?.image,
    imageColor: headerConfig?.imageColor
      ? processColor(headerConfig.imageColor)
      : null,
    title: headerConfig?.title,
    titleColor: headerConfig?.titleColor
      ? processColor(headerConfig.titleColor)
      : null,
  });
};

export { MenuView, showMenu };
export type { MenuComponentProps, MenuAction };
